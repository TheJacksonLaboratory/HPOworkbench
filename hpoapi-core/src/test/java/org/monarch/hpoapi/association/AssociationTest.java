package org.monarch.hpoapi.association;



import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.monarch.hpoapi.ontology.Prefix;
import org.monarch.hpoapi.ontology.TermID;
import org.monarch.hpoapi.types.ByteString;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.*;



/**
 * Created by peter on 03.04.17.
 */
public class AssociationTest {

    private static Association association1=null;
    private static Association association2=null;

    private static String association_line1="OMIM\t100050\t100050 AARSKOG SYNDROME, AUTOSOMAL DOMINANT\t\tHP:0000028\tOMIM:100050\tIEA\t\t\t\tO\t\t2009.07.24\tHPO:skoehler";

    private static String association_line2="OMIM\t100050\t100050 AARSKOG SYNDROME, AUTOSOMAL DOMINANT\t\tHP:0000175\tOMIM:100050\tTAS\t\t\t\tO\t\t2012.07.16\tHPO:probinson";


    @BeforeClass
    public static void setup() throws Exception {
        association1 = new Association(association_line1);
        association2 = new Association(association_line2);
    }


    @Test
    public void testHPOid1() {
        TermID tid = association1.getTermID();
        Prefix pre = new Prefix("HP");
        TermID expected = new TermID(pre, 28);
        Assert.assertEquals(expected,tid);
    }

    @Test
    public void testHPOid2() {
        TermID tid = association2.getTermID();
        Prefix pre = new Prefix("HP");
        TermID expected = new TermID(pre, 175);
        Assert.assertEquals(expected,tid);
    }


    @Test
    public void testObjectSymbol1() {
        ByteString dbo = association1.getDB_Object();
        ByteString expected = new ByteString("dunno");
        Assert.assertEquals(expected,dbo);
    }


}
