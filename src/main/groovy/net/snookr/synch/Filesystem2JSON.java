/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.snookr.synch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.snookr.model.FSImage;
import net.snookr.filesystem.Filesystem;
import net.snookr.db.Database;
import net.snookr.db.FSImageDAO;
import net.snookr.transcode.JSON;
import net.snookr.transcode.JSONZip;
import net.snookr.transcode.Partitioner;

/**
 *
 * @author daniel
 */
public class Filesystem2JSON {

    final File sourceDir;
    final String hostname;
    final Partitioner partitioner;
    final String zipName;

    public Filesystem2JSON(File sourceDir, String hostname,Partitioner partitioner) {
        this.sourceDir = sourceDir;
        this.hostname = hostname;
        this.partitioner = partitioner;
        //this.zipName = "" + hostname + "-" + partitioner.name + ".json.zip";
        this.zipName = "" + hostname + ".json.zip";
    }

    public void run() {
        Filesystem fs = new Filesystem();
        fs.setBaseDir(sourceDir);
        List<FSImage> list = fs.getFSImageList();

        //List<FSImage> predictor = readFromDB();
        List<FSImage> predictor = readJSON();

        // Make this abstract, add an api !
        System.out.println("Filesystem2JSON::List before: " + list.size());
        FilesystemSynch fss = new FilesystemSynch(list, predictor);
        fss.run();
        System.out.println("Filesystem2JSON::List after: " + list.size());
        writeJSON(list);

    }

    private List readJSON() {

        try {
            InputStream fis = new FileInputStream(zipName);
            Map map = new JSONZip().decode(fis, JSON.FSImageListType);
            List list = partitioner.toList(map);
            fis.close();
            return list;
        } catch (Exception ex) {
            Logger.getLogger(Filesystem2JSON.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new ArrayList();

    }

    private void writeJSON(List<FSImage> list) {
        Map<String, List> map = partitioner.toMap(list);

        try {
            OutputStream fos = new FileOutputStream(zipName);
            new JSONZip().encode(map, fos);
            fos.close();
        } catch (Exception ex) {
            Logger.getLogger(Filesystem2JSON.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /*
     * Temporary until db40 goes away
     */
    private List<FSImage> readFromDB() {
        Database db = new Database();
        FSImageDAO fsImageDAO = new FSImageDAO();
        FSImageDAO.setDatabase(db);

        Map dbMapByFileName = fsImageDAO.getMapByPrimaryKey();
        db.close();

        // part 1 - extract camera info
        List<FSImage> predictor = new ArrayList(dbMapByFileName.size());
        predictor.addAll(dbMapByFileName.values());
        return predictor;
    }
}

