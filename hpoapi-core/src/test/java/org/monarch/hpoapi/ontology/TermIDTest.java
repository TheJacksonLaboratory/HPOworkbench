package org.monarch.hpoapi.ontology;

import org.junit.Assert;
import org.junit.Test;
import org.monarch.hpoapi.types.ByteString;

import static org.junit.Assert.*;

/**
 * Created by peter on 25.01.17.
 */
public class TermIDTest {
    @Test
    public void getPrefix() throws Exception {
        TermID tid = new TermID(42);
        Prefix p = tid.getPrefix();
        Prefix expected = new Prefix("HP");
        Assert.assertEquals(expected,p);
    }

    @Test
    public void toStringTest() throws Exception {
        TermID tid = new TermID(42);
        String asstring = tid.toString();
        String expected="HP:0000042";
        Assert.assertEquals(expected,asstring);
    }

    @Test
    public void toByteString() throws Exception {
        TermID tid = new TermID(42);
        ByteString expected = new ByteString("HP:0000042");
        Assert.assertEquals(expected,tid.toByteString());
    }

}