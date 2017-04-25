package org.monarch.hpoapi.ontology;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.monarch.hpoapi.association.AssociationContainer;
import org.monarch.hpoapi.io.AssociationParser;
import org.monarch.hpoapi.io.OBOParser;
import org.monarch.hpoapi.io.ParserFileInput;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by peter on 24.04.17.
 */
public class OntologyTest {

    private static Ontology ontology;

    @BeforeClass
    public static void setup() throws Exception {
        AssociationContainer hpoAssociations;
        ClassLoader classLoader = OntologyTest.class.getClassLoader();
        String filename = classLoader.getResource("hp-smalltest.obo").getFile();
        OBOParser oboparser = new OBOParser(new ParserFileInput(filename));
        System.err.println(oboparser.doParse());
        TermContainer hpoTerms = new TermContainer(oboparser.getTermMap(), oboparser.getFormatVersion(), oboparser.getDate());

        String associationFile = classLoader.getResource("small-annotationfile1.tab").getFile();
        AssociationParser ap=new AssociationParser(new ParserFileInput(associationFile), hpoTerms);
        System.out.println("About to parse ap");
        System.out.print(ap.parse());
        hpoAssociations = new AssociationContainer(ap.getAssociations(),ap.getAnnotationMapping());
        ontology = Ontology.create(hpoTerms);
    }


    @Test
    public void testGetLeafTerms() {
        ArrayList<Term> leaves= ontology.getLeafTerms();
    // test that Abnormality of the cerebrospinal fluid HP:0002921 is a leaf term in the test ontology
        Prefix pre = new Prefix("HP");
        TermID tid = new TermID(pre, 2921);
        Term t = ontology.getTerm(tid);

        Assert.assertTrue(leaves.contains(t));
        // HP:0007077 Abnormality of the nervous system - not a leafe term
        tid = new TermID(pre,7077);
        t = ontology.getTerm(tid);

        Assert.assertFalse(leaves.contains(t));
    }

    @Test
    public void testGetLeafTermIDs() {
        Collection<TermID> leaves= ontology.getLeafTermIDs();
        // test that Abnormality of the cerebrospinal fluid HP:0002921 is a leaf term in the test ontology
        Prefix pre = new Prefix("HP");
        TermID tid = new TermID(pre, 2921);


        Assert.assertTrue(leaves.contains(tid));
        // HP:0007077 Abnormality of the nervous system - not a leafe term
        tid = new TermID(pre,7077);


        Assert.assertFalse(leaves.contains(tid));
    }

    @Test
    public void testGetTermsInTopologicalOrder() {
        ArrayList<Term> leaves= ontology.getTermsInTopologicalOrder();
        Assert.assertTrue(leaves.size()>0);
        Term t1 = leaves.get(0);
        // get the root term "All"
        Prefix pre = new Prefix("HP");
        TermID tid = new TermID(pre, 1);
        Term t = ontology.getTerm(tid);
        Assert.assertEquals(t,t1);

    }

    @Test
    public void testRootTerm() {
        Prefix pre = new Prefix("HP");
        TermID tid = new TermID(pre, 1);
        Assert.assertTrue(ontology.isRootTerm(tid));
    }

    @Test
    public void testRootTerm2() {
        Prefix pre = new Prefix("HP");
        TermID tid = new TermID(pre, 1);
        Term t = ontology.getTerm(tid);
        Term root = ontology.getRootTerm();
        Assert.assertEquals(root,t);
    }

    @Test
    public void testHighestID() {
        int max = ontology.maximumTermID();
        int expected = 12757;
        Assert.assertEquals(expected,max);
    }

    @Test
    public void textNumberOfTerms() {
        int n = ontology.getNumberOfTerms();
        int expected= 17;
        Assert.assertEquals(expected,n);
    }

}
