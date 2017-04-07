package org.monarch.hpoapi.association;



import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.monarch.hpoapi.ontology.Prefix;
import org.monarch.hpoapi.ontology.TermID;
import org.monarch.hpoapi.types.ByteString;


/**
 * Created by peter on 03.04.17.
 */
public class AssociationTest {

    private static Association association1=null;
    private static Association association2=null;

    private static String association_line1="OMIM\t100050\t100050 AARSKOG SYNDROME, AUTOSOMAL DOMINANT\t\tHP:0000028\tOMIM:100050\tIEA\t\tHP:0040283\t\tO\t\t2009.07.24\tHPO:skoehler";

    private static String association_line2="OMIM\t100050\t100050 AARSKOG SYNDROME, AUTOSOMAL DOMINANT\t\tHP:0000175\tOMIM:100050\tTAS\tHP:0003584\tHP:0040282\t\tO\t\t2012.07.16\tHPO:probinson";


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
    public void testGetDatabase() {
        ByteString db = association1.getDatabase();
        ByteString expected = new ByteString("OMIM");
        Assert.assertEquals(expected,db);
        db = association2.getDatabase();
        Assert.assertEquals(expected,db);
    }

    @Test
    public void testGetDBObjectID1() {
        ByteString obID = association1.getObjectID();
        ByteString expected = new ByteString("100050");
        Assert.assertEquals(expected,obID);
        obID = association2.getObjectID();
        Assert.assertEquals(expected,obID);
    }

    @Test
    public void testGetDBName() {
        ByteString name = association1.getName();
        ByteString expected = new ByteString("100050 AARSKOG SYNDROME, AUTOSOMAL DOMINANT");
        Assert.assertEquals(expected,name);
    }

    @Test
    public void testGetTermID1() {
        TermID tid = association1.getTermID();
        TermID expected = new TermID("HP:0000028");
        Assert.assertEquals(expected,tid);
        Prefix pre = new Prefix("HP");
        tid = new TermID(pre, 28);
        Assert.assertEquals(expected,tid);
    }

    @Test
    public void testGetTermID2() {
        TermID tid = association2.getTermID();
        Prefix pre = new Prefix("HP");
        TermID expected = new TermID(pre, 175);
        Assert.assertEquals(expected,tid);
    }


    @Test
    public void testGetDbReference() {
       ByteString ref = association1.getDBReference();
       ByteString expected = new ByteString("OMIM:100050");
       Assert.assertEquals(expected,ref);
    }

    @Test
    public void testGetEvidence() {
        ByteString evi = association1.getEvidence();
        ByteString expected = new ByteString("IEA");
        Assert.assertEquals(expected,evi);
        evi = association2.getEvidence();
        expected = new ByteString("TAS");
        Assert.assertEquals(expected,evi);
    }

    @Test
    public void testGetOnset1() {
        ByteString onset = association1.getOnset();
        ByteString expected = new ByteString("");
        Assert.assertEquals(expected,onset);
    }

    @Test
    public void testGetOnset2() {
        ByteString onset = association2.getOnset();
        /* HP:0003584 is late onset */
        ByteString expected = new ByteString("HP:0003584");
        Assert.assertEquals(expected,onset);
    }


    @Test
    public void testGetFrequency1() {
        ByteString freq = association1.getFrequency();
        ByteString expected = new ByteString("HP:0040283");
        Assert.assertEquals(expected,freq);
        freq = association2.getFrequency();
        expected = new ByteString("HP:0040282");
        Assert.assertEquals(expected,freq);
    }


}
