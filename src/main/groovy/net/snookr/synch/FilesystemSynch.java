/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.snookr.synch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.snookr.model.FSImage;
import net.snookr.util.Exif;
import net.snookr.util.MD5;

/**
 *
 * @author daniel
 * The Object of this class is to synchronize
 * the filesystem with a local json zip database
 *  produce a list of new, deleted, and modified entries.
 *  -algorithm:
 *   map predictor by primary key : fileName
 *   traverse new list-
 *      if present in predictor: remove from predictor map
 *          if modifed: --
 *          if unchanged
 *      else: new
 *   at end predictor map holds only deleted images
 *
 */
public class FilesystemSynch {

    final List<FSImage> list;
    final List<FSImage> predictor;
    private final boolean verbose = true;

    public enum ModificationState {

        UNMODIFIED, MODIFED, NEW, DELETED
    }

    public enum UpdateBehaviour {

        NEVER, ASNEEDED, ALWAYS
    }

    public FilesystemSynch(List<FSImage> list, List<FSImage> predictor) {
        this.list = list;
        this.predictor = predictor;
    }

    public void run() {
        Map<String, FSImage> predByprimary = new HashMap<String, FSImage>();
        for (FSImage predima : predictor) {
            predByprimary.put(predima.fileName, predima);
        }

        List<FSImage> newImages = new ArrayList<FSImage>();
        List<FSImage> modifiedImages = new ArrayList<FSImage>();
        List<FSImage> unchangesImages = new ArrayList<FSImage>();
        List<FSImage> deletedImages = new ArrayList<FSImage>();

        Map<ModificationState,Integer> countByCode = new HashMap<ModificationState, Integer>();
        for (FSImage fsima : list) {
            FSImage predima = predByprimary.get(fsima.fileName);
            ModificationState modCode = updateWithPredictor(fsima, predima);
            if (predima != null) {
                predByprimary.remove(fsima.fileName);
            }
            Integer count = countByCode.get(modCode);
            countByCode.put(modCode, (count!=null)?count+1:1);
        }
        for (FSImage fsima : predByprimary.values()) {
            Integer count = countByCode.get(ModificationState.DELETED);
            countByCode.put(ModificationState.DELETED, (count!=null)?count+1:1);
        }
        for (ModificationState mod:countByCode.keySet()){
            System.out.println(""+mod+" : "+countByCode.get(mod));
        }

    }
    UpdateBehaviour md5Behaviour = UpdateBehaviour.ASNEEDED; // include setter for behaviour
    UpdateBehaviour exifDateBehaviour = UpdateBehaviour.ASNEEDED; // include setter for behaviour
    UpdateBehaviour exifCameraBehaviour = UpdateBehaviour.ASNEEDED; // include setter for behaviour

    private ModificationState updateWithPredictor(FSImage fsima, FSImage predima) {
        boolean isNew = false;
        boolean isModified = false;

        File f = new File(fsima.fileName);

        if (predima == null) {
            isNew = isModified = true;
            System.out.println("predima null: " + isModified);
        } else {
            // attributes assumed to be set in fsima!
            if (!fsima.size.equals(predima.size)) {
                isModified = true;
            }
            if (!fsima.lastModified.equals(predima.lastModified)) {
                isModified = true;
            }
        }

        // ExifDate section -
        // we can use predictor, if ALL of theses hold
        //  no value already present : i.e. value present-> do not extract
        //  iModified == false: if isModified force extraction
        //  exifDateBehaviour != UpdateBehaviour.ALWAYS : i.e. ALWAY forces extraction
        //  if it (predictor) exists (!=null)
        if (fsima.taken == null && (isModified == false) && exifDateBehaviour != UpdateBehaviour.ALWAYS && predima != null && predima.taken != null) {
            fsima.taken = predima.taken;
        }
        //if still no value: extract (unless NEVER is set
        if (fsima.taken == null && exifDateBehaviour != UpdateBehaviour.NEVER) {
            fsima.taken = Exif.getExifDate(f);
            if (verbose) {
                System.out.println("extraced exif date " + fsima.taken + " " + f.getName());
            }
        }
        // modification flag: modified unless they both exist and are the same
        //   i.e. if doesn't exist, or is different -> isModified==true
        if (predima == null || predima.taken == null || (fsima.taken!=null && !fsima.taken.equals(predima.taken))) {
            isModified = true;
        }


        // Camera extraction - same logic as ExifDate
        if (fsima.camera == null && (isModified == false) && exifCameraBehaviour != UpdateBehaviour.ALWAYS && predima != null && predima.camera != null) {
            fsima.camera = predima.camera;
        }
        //if still no value: extract (unless NEVER is set
        if (fsima.camera == null && exifCameraBehaviour != UpdateBehaviour.NEVER) {
            fsima.camera = Exif.getCamera(f);
            if (verbose) {
                System.out.println("extracted exif camera " + fsima.camera + " " + f.getName());
            }
        }
        // modification flag: modified unless they both exist and are the same
        //   i.e. if doesn't exist, or is different -> isModified==true
        if (predima == null || predima.camera == null || (fsima.camera != null && !fsima.camera.equals(predima.camera))) {
            isModified = true;
        }


        // MD5 Section - same logic as above
        if (fsima.md5 == null && (isModified == false) && md5Behaviour != UpdateBehaviour.ALWAYS && predima != null && predima.md5 != null) {
            fsima.md5 = predima.md5;
        }
        //if still no value: extract (unless NEVER is set
        if (fsima.md5 == null && md5Behaviour != UpdateBehaviour.NEVER) {
            try {
                fsima.md5 = MD5.digest(f);
                if (verbose) {
                    System.out.println("calculated md5 " + fsima.md5 + " " + f.getName());
                }
            } catch (IOException ex) {
                Logger.getLogger(FilesystemSynch.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // modification flag: modified unless they both exist and are the same
        //   i.e. if doesn't exist, or is different -> isModified==true
        if (predima == null || predima.md5 == null || (fsima.md5 != null && !fsima.md5.equals(predima.md5))) {
            isModified = true;
        }

        ModificationState returnCode = ModificationState.UNMODIFIED;
        if (isModified) {
            returnCode = ModificationState.MODIFED;
        }
        if (isNew) {
            returnCode = ModificationState.NEW;
        }

        if (isModified && verbose) {
            System.out.println("should save: (" + returnCode + ") " + fsima);
        }

        return returnCode;
    }
}