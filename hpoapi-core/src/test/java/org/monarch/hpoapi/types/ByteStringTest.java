package org.monarch.hpoapi.types;

import com.google.common.primitives.Bytes;
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

    /*timmedSubstring should take "only " to "only" */
    @Test
    public void trimmedSubstring() throws Exception {
        ByteString b =  new ByteString("This is only a test");
        ByteString c = b.trimmedSubstring(8,13);
        Assert.assertEquals("only",c.toString());
    }

    @Test
    public void indexOf() throws Exception {
        ByteString b =  new ByteString("This is only a test");
        int i = b.indexOf("only");
        Assert.assertEquals(8,i);
    }

    @Test
    public void equals() throws Exception {
        ByteString b =  new ByteString("This is only a test");
        ByteString c =  new ByteString("This is only");
        ByteString d =  new ByteString("This is only a test");
        Assert.assertTrue(b.equals(d));
        Assert.assertFalse(b.equals(c));
    }

    @Test
    public void splitBySingleChar() throws Exception {
        ByteString b =  new ByteString("This%is%only%a%test");
        ByteString[] ar = b.splitBySingleChar('%');
        ByteString[] exp = {new ByteString("This"),new ByteString("is"),
                new ByteString("only"),
                new ByteString("a"),
                new ByteString("test")};
        Assert.assertArrayEquals(exp,ar);
    }

    @Test
    public void parseFirstInt() throws Exception {
        ByteString b = new ByteString("thenumber42bla");
        int i = ByteString.parseFirstInt(b);
        Assert.assertEquals(42,i);
    }
/* b is lexicographically in front of c */
    @Test
    public void compareTo() throws Exception {
        ByteString b = new ByteString("thenumber42bla");
        ByteString c =  new ByteString("This%is%only%a%test");
        Assert.assertTrue(c.compareTo(b)<0);
    }

}