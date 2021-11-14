package org.monarchinitiative.hpoworkbench.io;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HPOParserTest {

    private static String hpoSmallOboFilePath=null;

    @BeforeEach
    public void setup() {
        ClassLoader classLoader = HPOParserTest.class.getClassLoader();
        hpoSmallOboFilePath=classLoader.getResource("hp-smalltest.obo").getFile();
    }

    @Test
    public void testInputOntology() {
        HPOParser parser = new HPOParser(hpoSmallOboFilePath);
        assertNotNull(parser);
    }

}
