/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.snookr.couchdb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.snookr.model.FSImage;
import net.snookr.synch.Filesystem2JSON;
import net.snookr.transcode.JSON;
import net.snookr.transcode.JSONZip;
import net.snookr.transcode.PartitionFSImage;
import net.snookr.transcode.Partitioner;

/**
 *
 * @author daniel
 */
public class CouchTrial {

    final String hostname;
    final Partitioner partitioner;
    final String zipName;

    public CouchTrial(String hostname) {
        this.hostname = hostname;
        this.partitioner = PartitionFSImage.BY_DIRECTORY;
        //this.partitioner = PartitionFSImage.BY_YEARMONTH;
        this.zipName = "" + hostname + ".json.zip";
    }

    public void run() {

        REST rest = new REST();
        String dbinfo = rest.get("");
        System.out.println(String.format("  %s", dbinfo));

        /*
        String img1json = "{\"fileName\":\"/Volumes/Scratch42/photo/catou/2002_06_30/100-0065_IMG.JPG\",\"size\":1064329,\"md5\":\"756dc53578236007b5c1457cba8d9210\",\"lastModified\":\"2002-06-30 18:21:03\",\"taken\":\"2002-06-16 16:03:41\",\"camera\":\"Canon|Canon PowerShot S30\"}";
        String key1 = "756dc53578236007b5c1457cba8d9210";
        String putResult = rest.put(key1, img1json);
        System.out.println(String.format("  %s", putResult));
        if (rest != null) {
            return;
        }
         *
         */
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        Type genericObjectType = new TypeToken<Map<String, String>>() {
        }.getType();
        List<FSImage> fslist = readJSON();
        int count = 0;
        for (FSImage fsima : fslist) {
            count++;
            if (count > 100) {
                break;
            }
            //System.out.println(String.format("%s-%s", DateFormat.format(fsima.taken),fsima.md5));
            String json = gson.toJson(fsima);
            System.out.println(String.format("-->  %s", json));
            String saved = rest.put(fsima.md5,json);
            System.out.println(String.format("<--  %s", saved));
            /*
            Map mima = gson.fromJson(json, genericObjectType);
            System.out.println(String.format(" type <-- %s", mima.getClass().toString()));
            String rejson = gson.toJson(mima);
            System.out.println(String.format("  %s", rejson));
            System.out.println("");
             */
        }
        System.out.println("" + count + " ... " + fslist.size());
    }

    private List readJSON() {
        try {
            InputStream fis = new FileInputStream(zipName);
            Map map = new JSONZip().decode(fis, JSON.FSImageListType);
            List list = partitioner.toList(map);
            fis.close();
            return list;
        } catch (Exception ex) {
            Logger.getLogger(Filesystem2JSON.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList();
    }
}
