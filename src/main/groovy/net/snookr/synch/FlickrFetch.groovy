/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.snookr.synch

import groovy.xml.*
import groovy.util.slurpersupport .*
import java.text.SimpleDateFormat;

import net.snookr.flickr.Flickr;
import net.snookr.flickr.Photos;
import net.snookr.db.Database;
import net.snookr.db.FlickrImageDAO;
import net.snookr.db.FSImageDAO;
import net.snookr.util.Spawner;
import net.snookr.util.DateFormat;
import net.snookr.util.Progress;
import net.snookr.util.MD5;
import net.snookr.util.Exif;
import net.snookr.model.FlickrImage;

/**
 *
 * @author daniel
 *   What this script does:
 *   Fetches photos...
 *   getSizes returns data as:
 *   source: is the url for the image itself
 *   url: is a web page for that photo at that size
 *
 *   <sizes canblog="1" canprint="1" candownload="1">
 *   <size label="Square" width="75" height="75" source="http://farm1.static.flickr.com/145/419443247_34755ec3f3_s.jpg" url="http://www.flickr.com/photo_zoom.gne?id=419443247&amp;size=sq" />
 *   <size label="Thumbnail" width="100" height="75" source="http://farm1.static.flickr.com/145/419443247_34755ec3f3_t.jpg" url="http://www.flickr.com/photo_zoom.gne?id=419443247&amp;size=t" />
 *   <size label="Small" width="240" height="180" source="http://farm1.static.flickr.com/145/419443247_34755ec3f3_m.jpg" url="http://www.flickr.com/photo_zoom.gne?id=419443247&amp;size=s" />
 *   <size label="Medium" width="500" height="375" source="http://farm1.static.flickr.com/145/419443247_34755ec3f3.jpg" url="http://www.flickr.com/photo_zoom.gne?id=419443247&amp;size=m" />
 *   <size label="Large" width="1024" height="768" source="http://farm1.static.flickr.com/145/419443247_34755ec3f3_b.jpg" url="http://www.flickr.com/photo_zoom.gne?id=419443247&amp;size=l" />
 *   <size label="Original" width="2592" height="1944" source="http://farm1.static.flickr.com/145/419443247_1195f586b4_o.jpg" url="http://www.flickr.com/photo_zoom.gne?id=419443247&amp;size=o" />
 *   </sizes>
 *
 *  We will create tue urls ourselves instead:
 * http://farm{farm-id}.static.flickr.com/{server-id}/{id}_{secret}.jpg
 *	or
 * http://farm{farm-id}.static.flickr.com/{server-id}/{id}_{secret}_[mstb].jpg
 *	or
 * http://farm{farm-id}.static.flickr.com/{server-id}/{id}_{o-secret}_o.(jpg|gif|png)
 */
public class FlickrFetch implements Runnable {
    def verbose=false;
    File baseDir=null;
    void setBaseDir(File aBaseDir){
        baseDir = aBaseDir;
    }

    private List getFullFlickrList(){
        int getPhotoListThreads=10;
        List flickrList  = new Photos().getPhotoList(getPhotoListThreads);
        return flickrList;
    }

    public void seedScript(){
        Database db = new Database();
        FSImageDAO.setDatabase(db);
        FSImageDAO fsImageDAO = new FSImageDAO();
        Map dbMapByFileName = fsImageDAO.getMapByPrimaryKey();
        db.close();
        PrintStream ps = new PrintStream("seedScript.sh");
        dbMapByFileName.each() { fileName,fsima -> //
            String nuname = relativeStandardDirAndFileName(fsima.taken,fsima.md5);
            String path = new File(nuname).getParent();
            ps.println("mkdir -p ${path}");
            ps.println( "ln \"${fsima.fileName}\" ${nuname}" );
        }
        ps.close();
        System.exit(0);
    }
    public void run() {
        println "Hello Flickr Fetch";
        //seedScript();

        List photoList = getFullFlickrList();
        // sort the list
        photoList.sort(){ flima -> //
            -flima.taken.getTime();
        }
        // short the list
        //photoList = photoList[0..<20];

        int fetchPhotoThreads=1;
        Closure fetchPhotoClosure = { flima ->
            //String photoid = flima.photoid;
            //Map mapOfSizeUrls =  new Photos().getSizes(photoid);

            Map mapOfSizeUrls =  getSizes(flima);
            //println(flima);
            //println(mapOfSizeUrls);
            saveSizesToFiles(flima,mapOfSizeUrls);
        }
        new Spawner(photoList,fetchPhotoClosure,fetchPhotoThreads).run();

    }

    // Simulate Photos.getSizes
    Map getSizes(FlickrImage flima){
        /*
         *  We will create tue urls ourselves instead:
         * http://farm{farm-id}.static.flickr.com/{server-id}/{id}_{secret}.jpg
         *	or
         * http://farm{farm-id}.static.flickr.com/{server-id}/{id}_{secret}_[mstb].jpg
         *	or
         * http://farm{farm-id}.static.flickr.com/{server-id}/{id}_{o-secret}_o.(jpg|gif|png)
         * s	small square 75x75
         * t	thumbnail, 100 on longest side
         * m	small, 240 on longest side
         * -	medium, 500 on longest side
         * b	large, 1024 on longest side (only exists for very large original images)
         * o	original image, either a jpg, gif or png, depending on source format
         */
        // ["Square","Thumbnail","Small","Medium","Large","Original"];

        Map mapOfSizeUrls = [:];
        mapOfSizeUrls["Square"] =    "http://farm${flima.farm}.static.flickr.com/${flima.server}/${flima.photoid}_${flima.secret}_s.jpg";
        mapOfSizeUrls["Thumbnail"] = "http://farm${flima.farm}.static.flickr.com/${flima.server}/${flima.photoid}_${flima.secret}_t.jpg";
        mapOfSizeUrls["Small"] =     "http://farm${flima.farm}.static.flickr.com/${flima.server}/${flima.photoid}_${flima.secret}_m.jpg";
        mapOfSizeUrls["Medium"] =    "http://farm${flima.farm}.static.flickr.com/${flima.server}/${flima.photoid}_${flima.secret}.jpg";
        mapOfSizeUrls["Large"] =     "http://farm${flima.farm}.static.flickr.com/${flima.server}/${flima.photoid}_${flima.secret}_b.jpg";
        mapOfSizeUrls["Original"] =  "http://farm${flima.farm}.static.flickr.com/${flima.server}/${flima.photoid}_${flima.originalsecret}_o.jpg";
        return mapOfSizeUrls;
    }

    public File getBaseDirectory() {
        if (baseDir==null) {
            String homeDirPath = System.getProperty("user.home");
            File homeDir = new File(homeDirPath);
            if (!homeDir.exists()) {
                throw new Exception("Cannot Find HomeDir: "+homeDir);
            }
            File defaultBaseDir = new File(homeDir,"SnookrFetchDir");
            return defaultBaseDir;
        }
        return baseDir;
    }
    public void makeDir(File dir) {
        if(!dir.exists()){
            boolean success = dir.mkdirs();
            if (!success) {
                println("Directory creation failed: "+dir);
                throw new Exception("Directory creation failed: "+dir);
                return;
            }
        }
    }
    public void saveToFile(String urlStr,File newFile) {
        println "  url: ${urlStr} file:${newFile}";
        URL url = new URL(urlStr);
        BufferedInputStream inStream = new BufferedInputStream(url.openStream());
        FileOutputStream fos = new FileOutputStream(newFile);
        int read;
        while ((read = inStream.read()) != -1) {
            fos.write(read);
        }
        fos.flush();
        fos.close();
        inStream.close();
    }

    public void saveSizesToFiles(FlickrImage flima,Map mapOfSizeUrls) {
        //List listOfSizes = ["Square","Thumbnail","Small","Medium","Large","Original"];
        //List listOfSizes = ["Thumbnail","Square","Small"];
        //List listOfSizes = ["Square","Small"];
        //List listOfSizes = ["Square"];
        List listOfSizes = ["Thumbnail","Square","Small","Original"];

        File baseDir = getBaseDirectory();
        makeDir(baseDir);

        listOfSizes.each() { whichSize -> //
            File sizeDir = new File(baseDir,whichSize);
            // include year/year-month directory relative path
            String standardFileName = relativeStandardDirAndFileName(flima);
            File standardFile = new File(sizeDir,standardFileName);
            File pathToCreate = standardFile.getParentFile();
            makeDir(pathToCreate);
            if (!standardFile.exists()) {
                String url = mapOfSizeUrls[whichSize];
                if (url){
                    saveToFile(mapOfSizeUrls[whichSize],standardFile);
                }
            }
        }
    }

    /*  return a relative path and fileName:
     *   like  2003/2003-08/20030803142048-aefdee3f3a6250419cb5eb1c429b7fc9.jpg
     *   or 1970/1970-01/19700101000000-xxxxx.jpg
     */
    private String relativeStandardDirAndFileName(FlickrImage flima) {
        return relativeStandardDirAndFileName(flima.taken,flima.md5);
    }
    private String relativeStandardDirAndFileName(Date taken,String md5) {
        String datePart;
        try {
            SimpleDateFormat SDF = new SimpleDateFormat(YMDirYMDHMS);
            datePart =  SDF.format(taken);
        } catch (Exception e) {
            datePart=DEFAULTDATESTR;
        }
        return "${datePart}-${md5}.jpg";
    }
    private static final String YMDirYMDHMS = "yyyy/yyyy-MM/yyyyMMddHHmmss";
    private static final DEFAULTDATESTR="1970/1970-01/19700101000000";

}

