/**
 *
 * @author daniel
 */

package net.snookr.filesystem;

import net.snookr.model.FSImage;
import net.snookr.util.MD5;
import net.snookr.util.Exif;

public class Filesystem {
    boolean verbose=false;
    File baseDir=null;
    public void setBaseDir(File aBaseDir){
        baseDir = aBaseDir;
    }

    public List getFSImageList() {
        List fsImageList = [];
        List photoFileList = getPhotoFileList();
        photoFileList.each() { f -> //
            fsImageList << getFSImageFromFile(f);
        }
        return fsImageList;
    }

    FSImage getFSImageFromFile(File f) {
        FSImage fsima = new FSImage();

        fsima.fileName = f.getCanonicalPath();
        fsima.size = new Long(f.length());
        fsima.lastModified = new Date(f.lastModified());

        // only as needed... or depending on behaviour setting...
        // fsima.taken = Exif.getExifDate(f);
        // fsima.md5 = MD5.digest(f);
        return fsima
    }

    public List getPhotoFileList() {
        // Classify FileSystem walk the fileSystem and make
        // 4 lists: files[image|directory|skipped|other]
        Map files = [
            "image":[],
            "skipped":[],
            "directory":[],
            "other":[] 
        ];   

        baseDir.eachFileRecurse { f -> // examine each File
            if (f.isFile()) {
                String fileName = f.getName();
                if ( fileName.endsWith(".JPG") || fileName.endsWith(".jpg") ) {
                    files["image"] << f.getCanonicalFile();
                } else {
                    files["skipped"] << f.getCanonicalFile();
                }
            } else {
                // if not a directory what ?
                if (f.isDirectory()) {
                    //println "Directory: ${f.getCanonicalFile()}"
                    files["directory"] << f.getCanonicalFile();
                } else {
                    files["other"] << f;
                }
            }
        }

        if (verbose){
            println "fs classification"
            files.each() { k,v ->
                println "  ${k} : ${v.size()}"
            }
            Map areUnique = [:];
            files["image"].each() { f -> // examine each image File
                def cp = f.getPath();
                if (areUnique[cp]!=null) {
                    println "not unique ${cp} == ${areUnique[cp].getPath()}"
                }
                areUnique[cp]=f;
            }
            println "areUnique has size: ${areUnique.size()}"
        }

        return files["image"];
    }
}