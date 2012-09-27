/*
 * FSImageDAO.java
 *
 * Created on August 28, 2007, 2:45 AM
 *
 * Leave as groovy source because of dependancy on Database.groovy (till grovy-1.1 compiler!
 */

package net.snookr.db;

import net.snookr.model.FSImage;
import net.snookr.util.MD5;
import net.snookr.util.Exif;

/**
 *
 * @author daniel
 */
public class FSImageDAO {
    static int md5Never = 0;
    static int md5AsNeeded = 1; // if not already calculated
    static int md5Always = 2;
    int md5Behaviour = md5AsNeeded; // include setter for behaviour
    
    //TODO Proper Dependancy injection
    static Database db;
    static void setDatabase(Database aDatabase) {
        db = aDatabase;
    }
    static Database getDatabase() {
        return db;
    }
    
    /** Creates a new instance of FSImageDAO */
    public FSImageDAO() {
    }
    
    FSImage fetchForPrimaryKey(String fileName) {
        return (FSImage)getDatabase().fetchUniqueByValue(FSImage.class,"fileName",fileName);
    }

    List fetchForMD5(String md5) {
        return getDatabase().fetchByValue(FSImage.class,"md5",(Object)md5);
    }
    Map getMapByPrimaryKey() {
        Map dbMapByFileName = db.getMapForClassByPrimaryKey(FSImage.class,"fileName");
        //println "getMapForClassByPrimaryKey has ${dbMapByFileName.size()} entries"
        return dbMapByFileName;
    }

    public String createOrUpdate(FSImage fsima) {
        FSImage predictorFromDB = fetchForPrimaryKey(fsima.fileName);
        return createOrUpdateInternal(fsima,predictorFromDB);
    }

    /*
        if predictorFromDB is known use that. This allows for efficient fetching of many predictors
        simulatneously...
        if predictorFromDB is null, that means that it is known NOT to exist in the database
        return codes: Update,New,Unmodified
    */  
    private String createOrUpdateInternal(FSImage fsima,FSImage predictorFromDB) {
        // implement parse (attr) and persist photo info from flickr
        boolean isNew = false;
        boolean isModified = false;

        String fileName = fsima.fileName;
        File f = new File(fsima.fileName);

        FSImage persist = predictorFromDB;

        if (persist==null) {
            persist = fsima;
            isNew = isModified = true;
        } else {
            // attributes assumed to be set in fsima!
            if (fsima.size != persist.size) {
                persist.size=fsima.size;
                isModified=true;
            }
            if (fsima.lastModified != persist.lastModified) {
                persist.lastModified = fsima.lastModified
                isModified = true;
            }
        }

        // attributes not assumed to be set in fsima (because of cost...)
        // TODO behaviour thing like md5: always/never/asNeeded
        if (persist.taken==null) {
            Date taken = fsima.taken;
            if (!taken) {
                taken = Exif.getExifDate(f);
                println "extraced exif date ${taken} ${f.getName()}"
            }
            if (taken != persist.taken) {
                persist.taken = taken
                isModified = true;
            }
        }
        // attributes not assumed to be set in fsima (because of cost...)
        // TODO behaviour thing like md5: always/never/asNeeded
        if (persist.camera==null) {
            String camera = fsima.camera;
            if (!camera) {
                camera = Exif.getCamera(f);
                //println "extraced exif camera ${camera} ${f.getName()}"
            }
            if (camera != persist.camera) {
                persist.camera = camera
                isModified = true;
            }
        }

        if (  (md5Behaviour!=md5Never) && 
                  (persist.md5 == null || md5Behaviour == md5Always) ) {
            String md5 = fsima.md5;
            if (!md5) {
                md5 = MD5.digest(f);
                println "calculated md5 ${md5} ${f.getName()}"
            }
            if (md5 != persist.md5) {
                persist.md5 = md5;
                isModified=true;
            }
        }

        // ! syntax highlitee hates nested conditional expressions
        def returnCode = (isModified)? "Update":"Unmodified";
        if (isNew) returnCode="New";

        if (isModified) {
            db.save(persist);
            println "saved (${returnCode}) ${persist}";
        }
        return returnCode;
    }

}
