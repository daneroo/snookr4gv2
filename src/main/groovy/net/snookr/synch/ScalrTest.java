/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.snookr.synch;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.snookr.model.FSImage;
import net.snookr.scalr.CloudZip;
import net.snookr.transcode.JSON;
import net.snookr.transcode.JSONZip;
import net.snookr.util.MD5;

/**
 *
 * @author daniel
 */
public class ScalrTest {

    final String zipName;
    //static final String postZipURL = "http://localhost:8080/zip";
    static final String postZipURL = "http://scalr.appspot.com/zip";

    public ScalrTest(String zipName) {
        //this.zipName = "" + hostname + ".json.zip";
        this.zipName = zipName;
    }

    public void run() {

        // get the fs.json map
        // get the scalr map (manifest)
        // act on the fs.json map to PRESERVE DELETE.
        Map<String, List<FSImage>> fsJsonMap = readJSON();
        System.out.println("Loaded FS JSON Map: " + fsJsonMap.size());

        boolean alterInput = false;
        if (alterInput) {
            // remove half
            boolean toggle = true;
            List<FSImage> swapContent = null;
            List<String> keysToRemove = new ArrayList<String>();
            for (String key : fsJsonMap.keySet()) {
                if (toggle) {
                    keysToRemove.add(key);
                }
                toggle = !toggle;
            }
            for (String key : keysToRemove) {
                swapContent = fsJsonMap.remove(key);
            }
            // Now swap some(half of remaining) to provoke Add/REPLACE
            //List<String> keysToSwap = new ArrayList<String>();
            for (String key : fsJsonMap.keySet()) {
                if (toggle) {
                    //keysToRemove.add(key);
                    fsJsonMap.put(key, swapContent);
                }
                toggle = !toggle;
            }
        //for (String key : keysToRemove) {
        //    swapContent = fsJsonMap.remove(key);
        /// }

        }


        CloudZip cz = new CloudZip(postZipURL);
        byte[] manifestBytes = cz.manifest(zipName);
        showManifest("Loaded CloudZip Manifest: ",manifestBytes);

        Map<String, Map<String, String>> manifestMap = decodeManifestAsMap(manifestBytes);

        //Walk the fsJSonMap, marking for PRESERVE,DELETE, or default ADD_OR_REPLACE
        for (Map.Entry<String, List<FSImage>> e : fsJsonMap.entrySet()) {
            String name = e.getKey();
            List<FSImage> part = e.getValue();
            Map<String, String> manifestEntry = manifestMap.get(name);
            byte[] reencoded = new JSON().encode(part).getBytes();
            String md5 = MD5.digest(reencoded);
            String length = "" + reencoded.length;
            // PRESERVE TEST
            if (manifestEntry != null && md5.equals(manifestEntry.get("md5")) && length.equals(manifestEntry.get("length"))) {
                System.out.println("PRESERVE: " + name);
                // replace List by PRESERVE MARKER
                fsJsonMap.put(name, JSONZip.PRESERVE_MARKER_EMPTYLIST);
            } else {
                // ADD_OR_REPLACE
                System.out.println("REPLACE: " + name);
            }
            // remove the entry - so we know to delete remaining.
            if (manifestEntry != null) {
                manifestMap.remove(name);
            }
        }
        // manifestMap now contains entries to be deleted
        // This is actually not required, zip update with stream performs same logic.
        for (Map.Entry<String, Map<String, String>> e : manifestMap.entrySet()) {
            String name = e.getKey();
            Map<String, String> entry = e.getValue();
            System.out.println("DELETE: " + name);
            fsJsonMap.put(name, JSONZip.DELETION_MARKER_EMPTYLIST);
        }
        Map<String, List> lessTyped = new LinkedHashMap<String, List>(fsJsonMap);
        byte[] deltaZip = new JSONZip().encode(lessTyped);
        byte[] postResponse = cz.post(zipName, deltaZip);
        showManifest("POST result", postResponse);
    }

    private Map readJSON() {

        try {
            InputStream fis = new FileInputStream(zipName);
            Map map = new JSONZip().decode(fis, JSON.FSImageListType);
            fis.close();
            return map;
        } catch (Exception ex) {
            Logger.getLogger(Filesystem2JSON.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new LinkedHashMap<String, List<FSImage>>();

    }

    private List<Map<String, String>> decodeManifest(byte[] manifestBytes) {
        JSON json = new JSON();
        Type listType = JSON.ManifestEntryListType;
        List<Map<String,String>> list = json.decode(new String(manifestBytes), listType);
        return list;
    }

    private Map<String, Map<String, String>> decodeManifestAsMap(byte[] manifestBytes) {
        List<Map<String, String>> manifestList = decodeManifest(manifestBytes);
        Map<String, Map<String, String>> manifestMap = new LinkedHashMap<String, Map<String, String>>();
        for (Map<String, String> entry : manifestList) {
            //System.out.println(String.format("mapping -name:%s length:%s md5:%s", entry.get("name"), entry.get("length"), entry.get("md5")));
            manifestMap.put(entry.get("name"), entry);
        }
        return manifestMap;
    }

    private void showManifest(String msg,byte[] manifestBytes) {
        Map<String, Map<String, String>> manifestMap = decodeManifestAsMap(manifestBytes);
        System.out.println(msg+" - sz: " + manifestMap.size());
        for (Map.Entry<String, Map<String, String>> e : manifestMap.entrySet()) {
            String name = e.getKey();
            Map<String, String> entry = e.getValue();
            // here name = entry.get("name")
            System.out.println(String.format("-name:%s length:%s md5:%s", entry.get("name"), entry.get("length"), entry.get("md5")));
        }
    }
}
