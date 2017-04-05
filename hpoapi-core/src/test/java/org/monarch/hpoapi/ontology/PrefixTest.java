package org.monarch.hpoapi.ontology;

import org.junit.Assert;
import org.junit.Test;
import org.monarch.hpoapi.types.ByteString;

import static org.junit.Assert.*;

/**
 * Created by peter on 25.01.17.
 */
public class PrefixTest {
    @Test
    public void equals() throws Exception {
        Prefix p = new Prefix("HP");
        Assert.assertEquals(TermID.DEFAULT_PREFIX,p);
    }

    @Test
    public void toStringTest() throws Exception {
        Prefix p = new Prefix("HP");
        Assert.assertEquals("HP",p.toString());
    }

    @Test
    public void getByteString() throws Exception {
        Prefix p = new Prefix("HP");
        ByteString exp = new ByteString("HP");
        Assert.assertEquals(exp,p.getByteString());
    }

}