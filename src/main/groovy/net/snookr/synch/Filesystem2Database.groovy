/*
 * Filesystem2Database
 *
 */

package net.snookr.synch;
import net.snookr.filesystem.Filesystem;
import net.snookr.db.Database;
import net.snookr.db.FSImageDAO;
import net.snookr.util.Progress;
import net.snookr.model.FSImage;

/**
 *
 * @author daniel
 */
public class Filesystem2Database {

    File baseDir=null;
    void setBaseDir(File aBaseDir){
        baseDir = aBaseDir;
    }

    Database db=null;
    void setDatabase(Database aDatabase) {
        db = aDatabase;
    }

    public void run() {
        Filesystem fs = new Filesystem();
        fs.setBaseDir(baseDir);

        List fsImageList = fs.getFSImageList();

        // setup dao
        FSImageDAO.setDatabase(db);
        FSImageDAO fsImageDAO = new FSImageDAO();

        Progress pr = new Progress(fsImageList.size(),"ph",2000);

        Map returnCodes = [:];

        // see fs.groovy for example of using 
        // createOrUpdateInternal(fsima,dbPredictorByFilename[fsima.fileName]);
        fsImageList.each() { fsima -> // all flickr images
            def returnCode = fsImageDAO.createOrUpdate(fsima);
            def count = returnCodes[returnCode];
            returnCodes[returnCode] = (count==null)?1:(count+1);
            pr.increment();
        }
        returnCodes.each() { k,v -> // print histogram of return codes
            println "Filesystem<-->Database  ${k} : ${v}"
        }
    }
}
