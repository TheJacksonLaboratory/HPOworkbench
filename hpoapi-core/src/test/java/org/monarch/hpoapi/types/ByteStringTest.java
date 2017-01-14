package org.monarch.hpoapi.types;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by robinp on 1/13/17.
 */
public class ByteStringTest {
    @Test
    public void length() throws Exception {
        ByteString b = new ByteString("test");
        int len = b.length();
        Assert.assertEquals(4,len);
    }

    @Test
    public void toStringTest() throws Exception {
        ByteString b = new ByteString("test");
        Assert.assertEquals(b.toString(), "test");
    }

    @Test
    public void startsWith() throws Exception {
        ByteString b =  new ByteString("This is only a test");
        Assert.assertTrue(b.startsWith("This"));
        Assert.assertFalse(b.startsWith("That"));
    }

    @Test
    public void startsWithIgnoreCase() throws Exception {
        ByteString b =  new ByteString("This is only a test");
        Assert.assertTrue(b.startsWithIgnoreCase("this"));
    }

    /* Substring is inclusive (start) exclusive(end) with zero based indexing */
    @Test
    public void substring() throws Exception {
        ByteString b =  new ByteString("This is only a test");
        ByteString c = b.substring(8,12);
        Assert.assertEquals("only",c.toString());
    }

    @Test
    public void copyTo() throws Exception {
        ByteString b =  new ByteString("This is only a test");
        byte[] ar = new byte[4];
        b.copyTo(8,12,ar,0);
        ByteString c = new ByteString(ar);
        Assert.assertEquals("only",c.toString());
    }

    @Test
    public void isPrefixOf() throws Exception {
        ByteString b =  new ByteString("This");
        Assert.assertTrue(b.isPrefixOf("This is only a test"));
        Assert.assertFalse(b.isPrefixOf("Some other phrase"));
    }

    @Test
    public void contains() throws Exception {
        ByteString b =  new ByteString("This is only a test");
        Assert.assertTrue(b.contains("only"));
    }

    @Test
    public void trimmedSubstring() throws Exception {

    }

    @Test
    public void indexOf() throws Exception {

    }

    @Test
    public void indexOf1() throws Exception {

    }

    @Test
    public void equals() throws Exception {

    }

    @Test
    public void hashCodeTest() throws Exception {

    }

    @Test
    public void splitBySingleChar() throws Exception {

    }

    @Test
    public void parseFirstInt() throws Exception {

    }

    @Test
    public void parseFirstInt1() throws Exception {

    }

    @Test
    public void compareTo() throws Exception {

    }

    @Test
    public void b() throws Exception {

    }

    @Test
    public void toString1() throws Exception {

    }

}