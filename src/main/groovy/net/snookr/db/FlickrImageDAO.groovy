/*
 * FlickrImageDAO.java
 *
 * Created on August 28, 2007, 2:45 AM
 *
 * Leave as groovy source because of dependancy on Database.groovy (till grovy-1.1 compiler!
 */

package net.snookr.db;
import net.snookr.model.FlickrImage;

/**
 *
 * @author daniel
 */
public class FlickrImageDAO {
    
    //TODO Proper Dependancy injection
    static Database db;
    static void setDatabase(Database aDatabase) {
        db = aDatabase;
    }
    static Database getDatabase() {
        return db;
    }
    
    /** Creates a new instance of FlickrImageDAO */
    public FlickrImageDAO() {
    }
    
    FlickrImage fetchForPrimaryKey(String photoid) {
        return (FlickrImage)getDatabase().fetchUniqueByValue(FlickrImage.class,"photoid",photoid);
    }

    List fetchForMD5(String md5) {
        return getDatabase().fetchByValue(FlickrImage.class,"md5",(Object)md5);
    }
    Map getMapByPrimaryKey() {
        Map dbMapByPhotoid = db.getMapForClassByPrimaryKey(FlickrImage.class,"photoid");
        //println "getMapForClassByPrimaryKey has ${dbMapByPhotoid.size()} entries"
        return dbMapByPhotoid;
    }

    public String createOrUpdate(FlickrImage flima) {
        FlickrImage predictorFromDB = fetchForPrimaryKey(flima.photoid);
        return createOrUpdateInternal(flima,predictorFromDB);
    }

    public void delete(FlickrImage flima) {
        FlickrImage predictorFromDB = fetchForPrimaryKey(flima.photoid);
        db.delete(predictorFromDB);
    }
    /*
    if predictorFromDB is known use that. This allows for efficient fetching of many predictors
    simulatneously...
    if predictorFromDB is null, that means that it is known NOT to exist in the database
    return codes: Update,New,Unmodified
     */
    private String createOrUpdateInternal(FlickrImage flima,FlickrImage predictorFromDB) {
        // implement parse (attr) and persist photo info from flickr
        boolean isNew = false;
        boolean isModified = false;

        def photoid = flima.photoid;

        def persist = predictorFromDB;

        if (persist==null) {
            persist = flima;
            isNew = isModified = true;
        } else {
            if (persist.md5 != flima.md5) {
                persist.md5 = flima.md5;
                isModified = true;
            }
            if (persist.taken != flima.taken) {
                persist.taken = flima.taken;
                isModified = true;
            }
            if (persist.posted != flima.posted) {
                persist.posted = flima.posted;
                isModified = true;
            }
            if (persist.lastUpdate != flima.lastUpdate) {
                persist.lastUpdate = flima.lastUpdate;
                isModified = true;
            }
            // new fields for making fetch urls
            if (persist.farm != flima.farm) {
                persist.farm = flima.farm;
                isModified = true;
            }
            if (persist.server != flima.server) {
                persist.server = flima.server;
                isModified = true;
            }
            if (persist.secret != flima.secret) {
                persist.secret = flima.secret;
                isModified = true;
            }
            if (persist.originalsecret != flima.originalsecret) {
                persist.originalsecret = flima.originalsecret;
                isModified = true;
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
