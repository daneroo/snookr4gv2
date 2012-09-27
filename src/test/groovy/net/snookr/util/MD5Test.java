/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.snookr.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.*;

/**
 *
 * @author daniel
 */
@RunWith(Parameterized.class)
public class MD5Test {

    @Parameters
    public static Collection data() {
        return Arrays.asList(new Object[][]{
                    {"", "d41d8cd98f00b204e9800998ecf8427e"},
                    {"a", "0cc175b9c0f1b6a831c399e269772661"},
                    {"abc", "900150983cd24fb0d6963f7d28e17f72"},
                    {"message digest", "f96b697d7cb7938d525a2f31aaf161d0"},
                    {"abcdefghijklmnopqrstuvwxyz", "c3fcd3d76192e4007dfb496cca67e13b"},
                    {"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", "d174ab98d277d9f5a5611c2c9f419d9f"},
                    {"12345678901234567890123456789012345678901234567890123456789012345678901234567890", "57edf4a22be3c955ac49da2e2107b67a"},
                    {"snookr", "e085d567677b1d7bfe85a051772aca43"},
                });
    }
    private String param;
    private String expResultForParam;

    public MD5Test(String param, String expResultForParam) {
        this.param = param;
        this.expResultForParam = expResultForParam;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of digest method, of class MD5.
     */
    @Test
    public void testDigest_String() {
        System.out.println("digest String: "+param);
        String result = MD5.digest(param);
        assertEquals(expResultForParam, result);
    }

    /**
     * Test of digest method, of class MD5.
     */
    @Test
    public void testDigest_InputStream() throws Exception {
        System.out.println("digest Stream: "+param);
        InputStream is = new ByteArrayInputStream(param.getBytes());
        String result = MD5.digest(is);
        assertEquals(expResultForParam, result);
    }
}