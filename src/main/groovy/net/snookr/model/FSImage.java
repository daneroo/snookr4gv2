/*
 * FSImage.java
 */
package net.snookr.model;

import java.util.Date;
import java.io.File;
import net.snookr.util.DateFormat;

/**
 *
 * @author daniel
 */
public class FSImage {
    public String fileName; // canonical path - Natural (unique) key
    public Long size;
    public String md5;
    public Date lastModified;
    public Date taken;  // this is extracted from exif data when available - null if none available
    public String camera; // this is extracted from exif data when available - null if none available

    /** Creates a new instance of FSImage */
    public FSImage() {
    }

    public String toString() {
        //File f = new File(fileName);
        //return "file:" + f.getPath() + " sz:" + size + " md5:" + md5 + " mod:" + safeDate(lastModified) + " taken:" + safeDate(taken) + " camera:" + camera;
        return "file:" + fileName + " sz:" + size + " md5:" + md5 + " mod:" + safeDate(lastModified) + " taken:" + safeDate(taken) + " camera:" + camera;
    }

    String safeDate(Date d) {
        return DateFormat.format(d, "????-??-?? ??:??:??");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FSImage other = (FSImage) obj;
        if ((this.fileName == null) ? (other.fileName != null) : !this.fileName.equals(other.fileName)) {
            return false;
        }
        if (this.size != other.size && (this.size == null || !this.size.equals(other.size))) {
            return false;
        }
        if ((this.md5 == null) ? (other.md5 != null) : !this.md5.equals(other.md5)) {
            return false;
        }
        if (this.lastModified != other.lastModified && (this.lastModified == null || !this.lastModified.equals(other.lastModified))) {
            return false;
        }
        if (this.taken != other.taken && (this.taken == null || !this.taken.equals(other.taken))) {
            return false;
        }
        if ((this.camera == null) ? (other.camera != null) : !this.camera.equals(other.camera)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.fileName != null ? this.fileName.hashCode() : 0);
        hash = 97 * hash + (this.size != null ? this.size.hashCode() : 0);
        hash = 97 * hash + (this.md5 != null ? this.md5.hashCode() : 0);
        hash = 97 * hash + (this.lastModified != null ? this.lastModified.hashCode() : 0);
        hash = 97 * hash + (this.taken != null ? this.taken.hashCode() : 0);
        hash = 97 * hash + (this.camera != null ? this.camera.hashCode() : 0);
        return hash;
    }
}

