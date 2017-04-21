package org.monarch.hpoapi.io;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.monarch.hpoapi.ontology.TermContainer;
import org.monarch.hpoapi.ontology.TermMap;
import org.monarch.hpoapi.types.ByteString;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by robinp on 4/20/17.
 */
public class GAFByteLineScannerTest {

    private static IParserInput parserinput;

    private static GAFByteLineScanner scanner;

    @BeforeClass
    public static void setup() throws Exception {
        ClassLoader classLoader = GAFByteLineScannerTest.class.getClassLoader();
        String filename = classLoader.getResource("hp-smalltest.obo").getFile();
        OBOParser oboparser = new OBOParser(new ParserFileInput(filename));
        System.err.println(oboparser.doParse());
        TermContainer hpoTerms = new TermContainer(oboparser.getTermMap(), oboparser.getFormatVersion(), oboparser.getDate());

        String associationFile = classLoader.getResource("small-annotationfile1.tab").getFile();

        parserinput = new ParserFileInput(associationFile);
        //  GAFByteLineScanner(IParserInput input, byte [] head, Set<ByteString> names, TermMap terms, Set<ByteString> evidences, IAssociationParserProgress progress)
        TermMap terms =  TermMap.create(oboparser.getTermMap());
        HashSet<ByteString> names=null;
        Set<ByteString> evidences=null;
        byte [] head = new byte [10];
        IAssociationParserProgress progress=null;
        scanner = new GAFByteLineScanner(parserinput, head, names,  terms,  evidences, progress);
        scanner.scan();
    }

    @Test
    public void testParser() {
        int n = parserinput.getSize();
        //System.out.println("parser size = " + n);
        Assert.assertTrue((n>0));
    }

    @Test
    public void testScanner1() {
        int n = scanner.getNumberOfUsedTerms();
        n=scanner.getAssociations().size();
        System.out.println("n terms size = " + n);
        Assert.assertTrue((n>0));
    }



}
