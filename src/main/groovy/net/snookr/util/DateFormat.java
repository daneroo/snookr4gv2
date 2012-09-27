/*
 * DateFormat.java
 *
 * Created on August 25, 2007, 12:20 AM
 *
 * Centralized Date Parsing, for concurrency and uniformity.
 */

package net.snookr.util;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author daniel
 */
public final class DateFormat {
    
    public static String format(Date d) {
        return DateFormat.format(d,EPOCHSTR);
    }
    public static String format(Date d,String defaultValue) {
        return new DateFormat().internalFormat(d,defaultValue);
    }
    public static Date parse(String s) {
        return new DateFormat().internalParse(s,EPOCH);
    }
    public static Date parse(String s,Date defaultValue) {
        return new DateFormat().internalParse(s,defaultValue);
    }
    
    /** Creates a new instance of DateFormat */
    private DateFormat() {
    }
    
    private String internalFormat(Date d,String defaultValue) {
        try {
            return SDF.format(d);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    private Date internalParse(String s,Date defaultValue) {
        try {
            return SDF.parse(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    // this is not static for thread safety reasons.
    private final SimpleDateFormat SDF = new SimpleDateFormat(YYYMMDDHHMMSS);

    private static final String YYYMMDDHHMMSS = "yyyy-MM-dd HH:mm:ss";
    private static final String EPOCHSTR="1970-01-01 00:00:00";
    // this is a latch variable, the only reason it is not final, is the excpetion handling in the value literal.
    public static final Date EPOCH = new GregorianCalendar(1970,Calendar.JANUARY,1).getTime();
}
