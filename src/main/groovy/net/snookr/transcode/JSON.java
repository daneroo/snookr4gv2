/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.snookr.transcode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.snookr.model.FSImage;
import net.snookr.model.FlickrImage;

/**
 *
 * @author daniel
 * http://sites.google.com/site/gson/gson-user-guide#TOC-Using-Gson
 * test:
 *    List of FSImage|FlickrImage 
 *    
 */
public class JSON {

    private Gson gson;
    public static Type FSImageListType = new TypeToken<List<FSImage>>() {
    }.getType();
    public static Type FlickrImageListType = new TypeToken<List<FlickrImage>>() {
    }.getType();
    public static Type ManifestEntryListType = new TypeToken<List<Map<String,String>>>() {
    }.getType();

    public JSON() {
        this(true);
    }

    public JSON(boolean pretty) { // default false: compact
        GsonBuilder gsb = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
        if (pretty) {
            gsb.setPrettyPrinting();
        }
        gson = gsb.create();
    }

    public Gson getGson() {
        return gson;
    }

    public String encode(List l) {
        StringBuffer sb = new StringBuffer();
        encode(l, sb);
        return sb.toString();
    }

    public void encode(List l, Appendable writer) {
        gson.toJson(l, writer);
    }

    public void encode(List list, OutputStream os) {
        try {
            Writer writer = new OutputStreamWriter(os);
            encode(list, writer);
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(JSON.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List decode(InputStream is, Type listType) {
        List list = gson.fromJson(new InputStreamReader(is), listType);
        return list;
    }
    public List decode(Reader reader, Type listType) {
        List list = gson.fromJson(reader, listType);
        return list;
    }

    public List decode(String json, Type listType) {
        return decode(new StringReader(json),listType);
    }

}
