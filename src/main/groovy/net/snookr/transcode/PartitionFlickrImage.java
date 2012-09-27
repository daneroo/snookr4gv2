/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.snookr.transcode;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import net.snookr.model.FlickrImage;


/**
 *
 * @author daniel
 * act as a factory
 */
public class PartitionFlickrImage {

    public static final Partitioner<FlickrImage, String> BY_YEAR =
            new Partitioner<FlickrImage, String>("byYear", new FliTakenComparator(), new FliYearGrouper());
    public static final Partitioner<FlickrImage, String> BY_YEARMONTH =
            new Partitioner<FlickrImage, String>("byYearMonth", new FliTakenComparator(), new FliYearMonthGrouper());
}

class FliTakenComparator implements Comparator<FlickrImage> {

    public int compare(FlickrImage o1, FlickrImage o2) {
        return o1.taken.compareTo(o2.taken);
    }
}

class FliYearGrouper implements Partitioner.Grouper<FlickrImage, String> {

    SimpleDateFormat yyyyFmt = new SimpleDateFormat("yyyy");

    public String key(FlickrImage flima) {
        return yyyyFmt.format(flima.taken);
    }
}

class FliYearMonthGrouper implements Partitioner.Grouper<FlickrImage, String> {

    SimpleDateFormat yyyyMMFmt = new SimpleDateFormat("yyyy-MM");

    public String key(FlickrImage flima) {
        return yyyyMMFmt.format(flima.taken);
    }
}
