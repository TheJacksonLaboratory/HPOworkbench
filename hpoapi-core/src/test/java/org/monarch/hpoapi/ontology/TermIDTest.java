package org.monarch.hpoapi.ontology;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.monarch.hpoapi.types.ByteString;

import static org.junit.Assert.*;

/**
 * Created by peter on 25.01.17.
 */
public class TermIDTest {

    private static TermID tid=null;

    @BeforeClass
    public static void init() {
        Prefix pre = new Prefix("HP");
        tid = new TermID(pre, 42);
    }


    @Test
    public void getPrefix() throws Exception {
        Prefix p = tid.getPrefix();
        Prefix expected = new Prefix("HP");
        Assert.assertEquals(expected,p);
    }

    @Test
    public void toStringTest() throws Exception {
        String asstring = tid.toString();
        String expected="HP:0000042";
        Assert.assertEquals(expected,asstring);
    }

    @Test
    public void toByteString() throws Exception {
        ByteString expected = new ByteString("HP:0000042");
        Assert.assertEquals(expected,tid.toByteString());
    }

}