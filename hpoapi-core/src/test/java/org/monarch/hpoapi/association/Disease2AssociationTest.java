package org.monarch.hpoapi.association;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.monarch.hpoapi.io.AssociationParser;
import org.monarch.hpoapi.io.OBOParser;
import org.monarch.hpoapi.io.ParserFileInput;
import org.monarch.hpoapi.ontology.Prefix;
import org.monarch.hpoapi.ontology.TermContainer;
import org.monarch.hpoapi.ontology.TermID;

/**
 * Created by robinp on 4/24/17.
 */
public class Disease2AssociationTest {

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
    public void testHPOid1() {

        Assert.assertEquals(1,1);
    }
}
