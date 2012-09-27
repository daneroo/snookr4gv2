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
import net.snookr.transcode.JSON;
import net.snookr.transcode.JSONZip;
import net.snookr.transcode.Partitioner;
import net.snookr.transcode.PartitionFSImage;
import net.snookr.transcode.PartitionFlickrImage;
import net.snookr.util.Timer;
import net.snookr.model.FSImage
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
/**
 *
 * @author daniel
 *  The objective here is to encode/decode a List of [FSImage|FlickrImage] objects
 *   each encoded as JSON, and then zipped.
 *   The List needs to be split into parts for 2 reasons:
 *     - JSON encode decode performance,
 *     - The List parts will be the first level of incremental transport
 *    SO The List need to be reliably partitioned, and ordered.
 *     - Order by primaryKey split by partSize
 *     - Order by date, split by Year/Month
 *  These cand be specified in groovy by sort closure and Collection.groupBy Closure.
 *    - We will work in memory (byte[])
 *     List -> byte[] { zip (parts.json) }
 */
class ReadWriteJSON {


    // Here is the Test bootstrap.
    public void run() {

        println "Hello JSONZip Read-Write (${getHost()})"

        println("FS Partitioning Tests");
        def fspartitioners = [
            PartitionFSImage.BY_YEAR,
            PartitionFSImage.BY_YEARMONTH,
            PartitionFSImage.BY_DIRECTORY,
        ];
        fspartitioners.each() { partitioner -> //
            List list = readFSImageFromDB();
            showPartition(list,partitioner);

            String hostname = getHost();
            String zipName = "${hostname}-${partitioner.name}.json.zip"

            Map sortedMap = partitioner.toMap(list);

            Type listType = JSON.FSImageListType;//FlickrImageListType
            (1..1).each(){roundTripTimerTest(false,sortedMap,zipName,listType);}
            //(1..3).each(){roundTripTimerTest(false,sortedMap,zipName,listType);}
            //(1..3).each(){roundTripTimerTest(true,sortedMap,zipName,listType);}
        }

        
        println("Flickr Partitioning Tests");
        def flipartitioners = [
            PartitionFlickrImage.BY_YEAR,
            PartitionFlickrImage.BY_YEARMONTH,
        ];
        flipartitioners.each() { partitioner -> //
            List list = readFlickrImageFromDB();
            showPartition(list,partitioner);

            String zipName = "Flickr-${partitioner.name}.json.zip"

            Map sortedMap = partitioner.toMap(list);

            Type listType = JSON.FlickrImageListType
            (1..1).each(){roundTripTimerTest(false,sortedMap,zipName,listType);}
            //(1..3).each(){roundTripTimerTest(false,sortedMap,zipName,listType);}
            //(1..3).each(){roundTripTimerTest(true,sortedMap,zipName,listType);}
        }
        
    }

    private void showPartition(List list,Partitioner partitioner){
        println("Partitioner: ${partitioner.name}");
        Map sortedMap = partitioner.toMap(list);
        int i=0;
        sortedMap.each() { mappedval,coll -> //
            if (i<2 || i>sortedMap.size()-3){
                println("${String.format("%6d",coll.size())} : ${mappedval} ");
            } else {
                if (i==2) println("      ...");
            }
            i++;
        }
        println("${String.format("%6d",list.size())} : Total ");
        def sizes = sortedMap.collect { mappedval,coll -> coll.size() }
        println("sizes: ${sizes}");
        println("sizes min:${sizes.min()} max:${sizes.max()}");

    }

    private void roundTripTimerTest(boolean memoryBased,Map map,String zipName,Type listType){
        Timer tt = new Timer();
        tt.restart();
        byte[] b = null;
        if (memoryBased) {
            b = new JSONZip().encode(map);
        } else {
            OutputStream fos = new FileOutputStream(zipName);
            new JSONZip().encode(map,fos);
            fos.close();
        }
        def wtime = tt.diff();

        List list = null;
        tt.restart();
        if (memoryBased) {
            list = join(new JSONZip().decode(b,listType));
        } else {
            InputStream fis = new FileInputStream(zipName);
            list = join(new JSONZip().decode(fis,listType));
            fis.close();
        }

        def rtime = tt.diff();
        long size = (memoryBased)?b.length:new File(zipName).size();
        String memFlag = (memoryBased)?"M":"F";
        println("${memFlag} ${String.format("%20s",zipName)} write: ${wtime}s. read: ${rtime}s. -> ${list.size()} images ${size/1024.0} kB");
    }

    private List join(Map<String,List> map){
        List list = [];
        for (Map.Entry<String, List> e : map.entrySet()) {
            String name = e.getKey();
            List part = e.getValue();
            list.addAll(part);
        }
        return list;
    }

    public List readFSImageFromDB() {
        Database db = new Database();
        FSImageDAO fsImageDAO = new FSImageDAO();
        fsImageDAO.setDatabase(db);

        Map dbMapByFileName = fsImageDAO.getMapByPrimaryKey();
        db.close();

        // part 1 - extract camera info
        List list = [];
        dbMapByFileName.each() { fileName,fsima -> //
            list.add(fsima);
        }
        return list;
    }

    public List readFlickrImageFromDB() {
        Database db = new Database();
        FlickrImageDAO flickrImageDAO = new FlickrImageDAO();
        flickrImageDAO.setDatabase(db);

        Map dbMapByPhotoid = flickrImageDAO.getMapByPrimaryKey();
        db.close();

        // part 1 - extract camera info
        List list = [];
        dbMapByPhotoid.each() { photoid,flima -> //
            list.add(flima);
        }
        return list;
    }

    private String getHost() {
        try {
            //return java.net.InetAddress.getLocalHost().getCanonicalHostName();
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            Logger.getLogger(JSONZip.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


}

