/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.snookr.transcode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import net.snookr.model.FSImage;

/**
 *
 * @author daniel
 * act as a factory
 */
public class PartitionFSImage {

    public static final Partitioner<FSImage, String> BY_DIRECTORY =
            new Partitioner<FSImage, String>("byDir", new FSDirComparator(), new FSDirGrouper());
    public static final Partitioner<FSImage, String> BY_YEAR =
            new Partitioner<FSImage, String>("byYear", new FSTakenComparator(), new FSYearGrouper());
    public static final Partitioner<FSImage, String> BY_YEARMONTH =
            new Partitioner<FSImage, String>("byYearMonth", new FSTakenComparator(), new FSYearMonthGrouper());
}

class FSDirGrouper implements Partitioner.Grouper<FSImage, String> {

    public String key(FSImage fsima) {
        return new File(fsima.fileName).getParent().replaceAll("/", ":");
    }
}

class FSDirComparator implements Comparator<FSImage> {

    public int compare(FSImage o1, FSImage o2) {
        return o1.fileName.compareTo(o2.fileName);
    }
}

class FSTakenComparator implements Comparator<FSImage> {

    public int compare(FSImage o1, FSImage o2) {
        return o1.taken.compareTo(o2.taken);
    }
}

class FSYearGrouper implements Partitioner.Grouper<FSImage, String> {

    SimpleDateFormat yyyyFmt = new SimpleDateFormat("yyyy");

    public String key(FSImage fsima) {
        return yyyyFmt.format(fsima.taken);
    }
}

class FSYearMonthGrouper implements Partitioner.Grouper<FSImage, String> {

    SimpleDateFormat yyyyMMFmt = new SimpleDateFormat("yyyy-MM");

    public String key(FSImage fsima) {
        return yyyyMMFmt.format(fsima.taken);
    }
}
