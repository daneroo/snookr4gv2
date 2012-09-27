/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.snookr.filesystem

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author daniel
 */
public class TraversalMemoryTest {
    File baseDir = new File("/Volumes/DarwinScratch/photo/");
    long startTime;
    long startMem;
    public TraversalMemoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        startTime = System.currentTimeMillis();
        startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @After
    public void tearDown() {
    }

    public void reportTime(String s){
        float diff =  (System.currentTimeMillis()-startTime)/1000f;
        println(String.format("%30s took %7.3fs",s,diff))
    }

    @Test
    public void testEmptyRecurse() {
        int count=0;
        baseDir.eachFileRecurse { f -> // examine each File
            count++;
        }
        reportTime("empty recurse: ${count}")
    }
    @Test

    public void testListingRecurse() {
        List l = [];
        baseDir.eachFileRecurse { f -> // examine each File
            l << f
        }
        reportTime("listing recurse: ${l.size()}")
    }

    @Test
    public void testExistRecurse() {
        int count=0;
        baseDir.eachFileRecurse { f -> // examine each File
            if (f.exists()) count++;
        }
        reportTime("exist recurse: ${count}")
    }
    @Test
    public void testExistAndFleRecurse() {
        int count=0;
        baseDir.eachFileRecurse { f -> // examine each File
            if (f.exists() && f.isFile()) {
                    count++;
            }
        }
        reportTime("isFile recurse: ${count}")
    }

    @Test
    public void testExistAndJPGRecurse() {
        int count=0;
        baseDir.eachFileRecurse { f -> // examine each File
            if (f.exists() && f.isFile()) {
                String fileName = f.getName();
                if ( fileName.endsWith(".JPG") || fileName.endsWith(".jpg") ) {
                    count++;
                }
            }
        }
        reportTime("isJPG recurse: ${count}")
    }

    @Test
    public void testGetPhotoFileList() {
        Filesystem fs = new Filesystem();
        fs.setBaseDir(baseDir);
        List l = fs.getPhotoFileList();
        reportTime("photo list ${l.size()}")
    }

    @Test
    public void testGetFSImageList() {
        Filesystem fs = new Filesystem();
        fs.setBaseDir(baseDir);
        List l = fs.getFSImageList();
        reportTime("FSImage list ${l.size()}")
    }
}

