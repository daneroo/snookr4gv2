/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.snookr.scalr;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.snookr.util.Timer;

/**
 *
 * @author daniel
 */
public class CloudZip {

    private final String baseURL;
    private static final boolean verboseGETResult = false;
    private static final boolean verbosePOSTResult = false;

    public CloudZip(String baseURL) {
        this.baseURL = baseURL;
    }

    public byte[] post(String name, byte[] content) {
        Map postParams = new LinkedHashMap();
        //postParams.put("name", name);
        //postParams.put("content", content);
        postParams.put(name, content);

        Timer tt = new Timer();
        ScalrImpl scalr = new ScalrImpl();
        byte[] postResult = scalr.postMultipart(baseURL, postParams);
        if (verbosePOSTResult) {
            printResult(postResult);
        }
        return postResult;
    }

    public byte[] manifest(String name) {
        return get(name, true);
    }
    /*
    public byte[] get(String name) {
    return get(name, false, false);
    }

    public byte[] delete(String name) {
    return get(name, false, true);
    }
     */

    private byte[] get(String name, boolean manifest) {
        Map getParams = new LinkedHashMap();
        getParams.put("name", name);
        if (manifest) {
            getParams.put("manifest", "manifest");
        }
        ScalrImpl scalr = new ScalrImpl();
        Timer tt = new Timer();
        byte[] getResult = scalr.get(baseURL, getParams);
        if (verboseGETResult) {
            printResult(getResult);
        }
        return getResult;
    }

    private void printResult(byte[] result) {
        System.out.println("  Result: -=-=-=");
        try {
            System.out.write(result);
        } catch (IOException ex) {
            Logger.getLogger(CloudZip.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("-=-=-=-=-=-=-=-= ");
    }
}
