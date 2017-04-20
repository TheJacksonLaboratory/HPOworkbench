package org.monarch.hpoapi.ontology;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.monarch.hpoapi.util.ParserFileInput;

import static org.junit.Assert.*;

/**
 * Created by robinp on 3/8/17.
 */
public class OBOParserTest {


    static private OBOParser oboparser=null;

    @BeforeClass
    public static void setup() throws Exception {
        ClassLoader classLoader = OBOParserTest.class.getClassLoader();
        String filename = classLoader.getResource("hp-smalltest.obo").getFile();
        oboparser = new OBOParser(new ParserFileInput(filename));
        System.err.println(oboparser.doParse());



    }

    /** Test that we get at least one term back.*/
    @Test
    public void testTest() {
        Set<Term> terms =oboparser.getTermMap();
        System.out.println("Getting back size = "+terms.size());
        Assert.assertTrue(terms.size()>0);
    }





}