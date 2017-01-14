package org.monarch.hpoapi.ontology;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by robinp on 1/15/17.
 */
public class TermXrefTest {
    @Test
    public void equals() throws Exception {

    }

    @Test
    public void getDatabase() throws Exception {
      TermXref txr = new  TermXref("UMLS", "C1844734", "Asymmetric lower limb shortness");
        Assert.assertEquals("UMLS",txr.getDatabase());
    }

    @Test
    public void getXrefId() throws Exception {
        TermXref txr = new  TermXref("UMLS", "C1844734", "Asymmetric lower limb shortness");
        Assert.assertEquals("C1844734",txr.getXrefId());
    }

    @Test
    public void getXrefName() throws Exception {
        TermXref txr = new  TermXref("UMLS", "C1844734", "Asymmetric lower limb shortness");
        Assert.assertEquals("Asymmetric lower limb shortness",txr.getXrefName());
    }

    @Test
    public void toStringTest() throws Exception {
        TermXref txr = new  TermXref("UMLS", "C1844734", "Asymmetric lower limb shortness");
        String res = txr.toString();
        Assert.assertEquals("UMLS - C1844734 - Asymmetric lower limb shortness",res);
    }

}