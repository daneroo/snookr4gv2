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
class SymmetricDiffs  implements Runnable {
    File baseDir=null;
    void setBaseDir(File aBaseDir){
        baseDir = aBaseDir;
    }
    public void run() {
        def verbose=false;
        println "Hello Symmetric Diffs"
        if (baseDir==null) {
            println "No baseDir specified"
            return;
        }

        Database db = new Database();

        def fs2db = new Filesystem2Database();
        fs2db.setBaseDir(baseDir);
        fs2db.setDatabase(db);
        fs2db.run();

        def flickr2db = new Flickr2Database();
        flickr2db.setDatabase(db);
        flickr2db.run();

        /*
        Diffs algorithm
        find all FSImages in db that are
        -still present on disk
        -not present in FlickrImage table.
         */

        FSImageDAO fsImageDAO = new FSImageDAO();
        fsImageDAO.setDatabase(db);
        FlickrImageDAO flickrImageDAO = new FlickrImageDAO();
        flickrImageDAO.setDatabase(db);
        Photos photos = new Photos();

        Map dbMapByFileName = fsImageDAO.getMapByPrimaryKey();
        Map dbMapByPhotoid = flickrImageDAO.getMapByPrimaryKey();

        // part 1 - fs uniqueness by md5
        Map fsimaUniqueByMd5 = [:];
        dbMapByFileName.each() { fileName,fsima -> //
            def md5 = fsima.md5;
            if (fsimaUniqueByMd5[md5]!=null) {
                if (verbose) println "not unique ${md5} == ${fsimaUniqueByMd5[md5].fileName}";
            }
            fsimaUniqueByMd5[md5]=fsima;
        }
        println "fsimaUniqueByMd5 has size: ${fsimaUniqueByMd5.size()}"

        // part 2 - flickr uniqueness by md5
        Map flickrimaUniqueByMd5 = [:];
        dbMapByPhotoid.each() { photoid,flickrima -> //
            def md5 = flickrima.md5;
            if (flickrimaUniqueByMd5[md5]!=null) {
                if (verbose) println "not unique ${md5} == ${flickrimaUniqueByMd5[md5].photoid}";
            }
            flickrimaUniqueByMd5[md5]=flickrima;
        }
        println "flickrimaUniqueByMd5 has size: ${flickrimaUniqueByMd5.size()}"

        // part 3 - in fs and not on flickr
        int totalNotOnFlickr=0;
        fsimaUniqueByMd5.each() { md5,fsima -> //
            if (flickrimaUniqueByMd5[md5]==null) {
                if (verbose) println "not on flickr: md5:${md5} -> ${fsima.fileName}";
                totalNotOnFlickr++;
            }
        }
        println "not on flickr total:  ${totalNotOnFlickr}";

        // part 4 - on flickr and not in fs
        int totalNotInFS=0;
        flickrimaUniqueByMd5.each() { md5,flickrima -> //
            if (fsimaUniqueByMd5[md5]==null) {
                if (verbose) println "not in fs: md5:${md5} -> ${flickrima.photoid}";
                totalNotInFS++;
            }
        }
        println "not in filesystem (lost?) total:  ${totalNotInFS}";

        // part 5 - upload missing files.
        int total=0;
        int totalFound=0;
        int totalMissing=0;
        def filesToUpload =[];
        dbMapByFileName.each() { fileName,fsima -> //
            //println("fn:${fileName} -> fsima:${fsima}");
            //println "fn:${fileName} looking for md5:${fsima.md5}";
            List found = flickrImageDAO.fetchForMD5(fsima.md5);
            if (found && found.size()>0) {
                totalFound++;
                if (found.size()>1) {
                    println "duplicates found on flickr for md5: ${fsima.md5}";
                    found.each() { flickrima -> //
                        println "   ${fsima.md5} -> flickr: ${flickrima.md5} ${flickrima.photoid}";
                    }
                }
            } else {
                if (verbose) println "fn:${fileName} flickr doesn't contain md5:${fsima.md5}";
                totalMissing++;

                File f = new File(fileName);
                if (f.exists()) {
                    filesToUpload.add(f);
                    /*
                    long start = new Date().getTime();
                    long nuPhotoid = photos.uploadPhoto(f);
                    long elapsedms = new Date().getTime() - start;
                    println "new photoid: ${nuPhotoid} uploaded in ${elapsedms/1000f}s.";
                     */
                } else {
                    println "Missing file not found in filesystem. cannot upload";
                }

            }
            total++;
        }
        println "examined ${total} FSImages, found:${totalFound} toUpload:${totalMissing}";

        // Reverse upload after
        filesToUpload = filesToUpload.reverse();
        
        println "uploading ${filesToUpload.size()} photos";
        /*
        println "uploading: ${filesToUpload[0]}";
        println "uploading: ${filesToUpload[1]}";
        println "  ...";
        println "uploading: ${filesToUpload[filesToUpload.size()-2]}";
        println "uploading: ${filesToUpload[filesToUpload.size()-1]}";
         */
        filesToUpload.each() { f -> // file objects accumulated above
            long start = new Date().getTime();
            long nuPhotoid = photos.uploadPhoto(f);
            long elapsedms = new Date().getTime() - start;
            println "new photoid: ${nuPhotoid} uploaded in ${elapsedms/1000f}s.";
        }

        //println "-=-=-= Database Summary:  =-=-=-"
        //db.printSummary(false);
        println "-=-=-= Close Database:  =-=-=-"
        db.close();
    }
	
}

