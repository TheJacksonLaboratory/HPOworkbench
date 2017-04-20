package org.monarch.hpoapi.association;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.monarch.hpoapi.ontology.OBOParser;
import org.monarch.hpoapi.ontology.TermContainer;
import org.monarch.hpoapi.util.IParserInput;
import org.monarch.hpoapi.util.ParserFileInput;

import java.util.ArrayList;

/**
 * Created by robinp on 4/20/17.
 */
public class AssociationParserTest {

    private static AssociationParser ap;


    @BeforeClass
    public static void setup() throws Exception {
        ClassLoader classLoader = AssociationParserTest.class.getClassLoader();
        String filename = classLoader.getResource("hp-smalltest.obo").getFile();
        OBOParser oboparser = new OBOParser(new ParserFileInput(filename));
        System.err.println(oboparser.doParse());
        TermContainer hpoTerms = new TermContainer(oboparser.getTermMap(), oboparser.getFormatVersion(), oboparser.getDate());

        String associationFile = classLoader.getResource("small-annotationfile1.tab").getFile();
        ap=new AssociationParser(new ParserFileInput(associationFile), hpoTerms);
        System.out.print(ap.parse());
        /*
        AssociationParser ap = new AssociationParser(new OBOParserFileInput(args.associationFile),goTerms,populationSet.getAllGeneNames(),
                new IAssociationParserProgress() {
                    private int max;
                    private long startTime;

                    public void init(int max)
                    {
                        this.max = max;
                        this.startTime = System.currentTimeMillis();
                    }

                    public void update(int current)
                    {
                        long currentTime = System.currentTimeMillis();

                        if (currentTime - startTime > 20000)
                        {
							//
                            System.err.print("\033[1A\033[K");
                            System.err.println("Reading annotation file: " + String.format("%.1f%%",current / (double)max * 100));
                        }
                    }

                    @Override
                    public void warning(String message)
                    {

                    }

                });
       */

    }


    @Test
    public void testGetAssociations() {
       ArrayList<Association> lst = ap.getAssociations();
       int n = lst.size();

        Assert.assertTrue((n>0));
    }


}
