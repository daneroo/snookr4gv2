/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.snookr.synch

import net.snookr.db.Database;
import net.snookr.db.FSImageDAO;
import net.snookr.db.FlickrImageDAO;
import net.snookr.flickr.Photos;
import net.snookr.flickr.Flickr;
import net.snookr.synch.Filesystem2Database;
import net.snookr.synch.Flickr2Database;

/**
 *
 * @author daniel
 */
class FixFlickrPostedDates implements Runnable {
    public void run() {
        def verbose=false;
        println "Hello fixFlickrPostedDates"

        Database db = new Database();

        def flickr2db = new Flickr2Database();
        flickr2db.setDatabase(db);
        flickr2db.run();


        FlickrImageDAO flickrImageDAO = new FlickrImageDAO();
        flickrImageDAO.setDatabase(db);
        Photos photos = new Photos();
        Flickr ff = new Flickr();

        Map dbMapByPhotoid = flickrImageDAO.getMapByPrimaryKey();


        // part 1 - flickr uniqueness by md5
        Map flickrimaUniqueByMd5 = [:];
        dbMapByPhotoid.each() { photoid,flickrima -> //
            def md5 = flickrima.md5;
            if (flickrimaUniqueByMd5[md5]!=null) {
                println "not unique ${md5} == ${flickrimaUniqueByMd5[md5].photoid}";
            }
            flickrimaUniqueByMd5[md5]=flickrima;
        }
        println "dbMapByPhotoid has size: ${dbMapByPhotoid.size()}"
        println "flickrimaUniqueByMd5 has size: ${flickrimaUniqueByMd5.size()}"

        // part 2 - check flickr dates
        int totalFlickrDatesToFix=0;
        int totalNotInFS=0;
        def fmt = new net.snookr.util.DateFormat();

        dbMapByPhotoid.each() { photoid,flickrima -> //
            //println "flickr: photoid:${photoid} -> ${flickrima.photoid} ${fmt.format(flickrima.taken)} ${fmt.format(flickrima.posted)}";

            if (flickrima.taken != flickrima.posted) {
                println "Fix: ${flickrima.photoid} ${fmt.format(flickrima.taken)} ${fmt.format(flickrima.posted)}";
                totalFlickrDatesToFix++;

                if (totalFlickrDatesToFix<=100) {
                    String rsp  = ff.setPostedDate(flickrima.photoid,flickrima.taken);
                    println "";
                    println rsp;
                    println "";
                }

            }
        }
        println "total dates to fix:  ${totalFlickrDatesToFix} out of ${dbMapByPhotoid.size()}";


        //println "-=-=-= Database Summary:  =-=-=-"
        //db.printSummary(false);
        println "-=-=-= Close Database:  =-=-=-"
        db.close();

    }
}

