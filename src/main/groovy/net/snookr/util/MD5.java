/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.snookr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author daniel
 */
public class MD5 {

    public static String digest(File f) throws IOException {
        InputStream is = new FileInputStream(f);
        String digest = digest(is);
        is.close();
        return digest;
    }

    public static String digest(String s) {
        return digest(s.getBytes());
    }

    public static String digest(byte[] b) {
        MessageDigest md = getImplementation();
        return toHex(md.digest(b));
    }

    public static String digest(InputStream is) throws IOException {
        MessageDigest md = getImplementation();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = is.read(buffer)) != -1) {
            md.update(buffer, 0, read);
        }
        return toHex(md.digest());
    }

    private static String toHex(byte[] hash) {
        StringBuffer buf = new StringBuffer(hash.length * 2);
        for (byte b : hash) {
            int intVal = b & 0xff;
            if (intVal < 0x10) {
                // append a zero before a one digit hex
                // number to make it two digits.
                buf.append("0");
            }
            buf.append(Integer.toHexString(intVal));
        }
        return buf.toString();
    }
    private static final String algorithm = "MD5";

    private static MessageDigest getImplementation() {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(nsae.getMessage());
        }
    }
}
