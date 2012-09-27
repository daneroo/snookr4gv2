package net.snookr.model;

import java.util.Date;
import net.snookr.util.DateFormat;

public class FlickrImage {
    public String photoid; // Foreign Natural (unique) key
    public String md5;
    public Date taken;   // data seeded from exif data / dan be modified...
    public Date posted;  // data of original post to flickr/can be modified...
    public Date lastUpdate; // last modification to any metadata/ includes tags,comments,etc...
    // added these to reconstruct fetch urls
    public String farm;
    public String server;
    public String secret;
    public String originalsecret; // original format is assumed to be jpg (not gif/png}

    /** Creates a new instance of FlickrImage */
    public FlickrImage() {
    }

    public String toString() {
        return "id:" + photoid + " md5:" + md5 + " taken:" + safeDate(taken) + " posted:" + safeDate(posted) + " lastUpdate:" + safeDate(lastUpdate);
    }

    String safeDate(Date d) {
        return DateFormat.format(d, "????-??-?? ??:??:??");
    }
}

