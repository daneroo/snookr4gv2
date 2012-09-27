/*
 * Flickr2Database
 *
 */

package net.snookr.synch;
import net.snookr.flickr.Flickr;
import net.snookr.flickr.Photos;
import net.snookr.db.Database;
import net.snookr.db.FlickrImageDAO;
import net.snookr.util.Progress;
import net.snookr.model.FlickrImage;

/**
 *
 * @author daniel
 */
public class Flickr2Database {

    Database db=null;
    void setDatabase(Database aDatabase) {
        db = aDatabase;
    }

    static final int getPhotoListThreads=10;
    List getFlickrPhotoList() {
        return new Photos().getPhotoList(getPhotoListThreads);
    }

    public void run() {
        // get Photos from flickr.
        List flickrList  = getFlickrPhotoList();

        // setup dao
        FlickrImageDAO.setDatabase(db);
        FlickrImageDAO flickrImageDAO = new FlickrImageDAO();

        Progress pr = new Progress(flickrList.size(),"ph",4000);
        Map returnCodes = [:];

        // see flickrSynch for example of using 
        // createOrUpdateInternal(flima,dbPredictorByPhotoid[flima.photoid]);
        flickrList.each() { flima -> // all flickr images
            def returnCode = flickrImageDAO.createOrUpdate(flima);
            def count = returnCodes[returnCode];
            returnCodes[returnCode] = (count==null)?1:(count+1);
            pr.increment();
        }
        returnCodes.each() { k,v -> // print histogram of return codes
            println "Flickr<-->Database  ${k} : ${v}"
        }
    }
}
