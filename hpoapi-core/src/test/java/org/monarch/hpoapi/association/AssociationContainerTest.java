package org.monarch.hpoapi.association;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.monarch.hpoapi.io.AssociationParser;
import org.monarch.hpoapi.io.OBOParser;
import org.monarch.hpoapi.io.ParserFileInput;
import org.monarch.hpoapi.ontology.TermContainer;
import org.monarch.hpoapi.types.ByteString;

/**
 * Created by robinp on 4/24/17.
 */
public class AssociationContainerTest {

    private static AssociationParser ap;
    private static AssociationContainer hpoAssociations;

    @BeforeClass
    public static void setup() throws Exception {
        ClassLoader classLoader = AssociationParserTest.class.getClassLoader();
        String filename = classLoader.getResource("hp-smalltest.obo").getFile();
        OBOParser oboparser = new OBOParser(new ParserFileInput(filename));
        System.err.println(oboparser.doParse());
        TermContainer hpoTerms = new TermContainer(oboparser.getTermMap(), oboparser.getFormatVersion(), oboparser.getDate());

        String associationFile = classLoader.getResource("small-annotationfile1.tab").getFile();
        ap=new AssociationParser(new ParserFileInput(associationFile), hpoTerms);
        System.out.println("About to parse ap");
        System.out.print(ap.parse());
        hpoAssociations = new AssociationContainer(ap.getAssociations(),ap.getAnnotationMapping());
    }


    @Test
    public void testDisease1() {
        ByteString diseaseID = new ByteString("999999");
        Disease2Association d2a = hpoAssociations.get(diseaseID);
        int sz = d2a.getAssociations().size();
        int expected = 4;
        Assert.assertEquals(expected,sz);
    }

    @Test
    public void testDisease2() {
        ByteString diseaseID = new ByteString("999999");
        Assert.assertTrue(hpoAssociations.isObjectID(diseaseID));
    }


}
