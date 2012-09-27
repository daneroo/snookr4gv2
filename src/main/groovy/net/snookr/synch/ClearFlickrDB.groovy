/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.snookr.synch

import net.snookr.db.Database;
import net.snookr.db.FSImageDAO;
import net.snookr.db.FlickrImageDAO;
import net.snookr.flickr.Photos;
import net.snookr.synch.Filesystem2Database;
import net.snookr.synch.Flickr2Database;


/**
 *
 * @author daniel
 */
public class ClearFlickrDB {
    public void run() {
        report();
        deleteAll();
        report();
    }
    public void deleteAll() {
        def verbose=false;
        println "Deleteing all flickr entries in DB";
        Database db = new Database();
        FlickrImageDAO.setDatabase(db);
        FlickrImageDAO flickrImageDAO = new FlickrImageDAO();

        Map dbMapByPhotoid = flickrImageDAO.getMapByPrimaryKey();

        Map flickrimaUniqueByMd5 = [:];
        dbMapByPhotoid.each() { photoid,flickrima -> //
                if (verbose) println("deleting ${flickrima}}");
                //flickrImageDAO.createOrUpdate(flickrima);
                flickrImageDAO.delete(flickrima);
        }
        db.close();
    }
    public void report() {
        def verbose=true;
        println "Hello ClearFlickrDB";
        Database db = new Database();
        FlickrImageDAO.setDatabase(db);
        FlickrImageDAO flickrImageDAO = new FlickrImageDAO();
        Map dbMapByPhotoid = flickrImageDAO.getMapByPrimaryKey();

        // part 2 - flickr uniqueness by md5
        Map flickrimaUniqueByMd5 = [:];
        dbMapByPhotoid.each() { photoid,flickrima -> //
            def md5 = flickrima.md5;
            if (flickrimaUniqueByMd5[md5]!=null) {
                if (verbose) println "not unique md5:${md5} == photoid:${flickrimaUniqueByMd5[md5].photoid}";
            }
            flickrimaUniqueByMd5[md5]=flickrima;
        }
        println "flickrimaUniqueByMd5 has size: ${flickrimaUniqueByMd5.size()}"
        db.close();
    }

}
