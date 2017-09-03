import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.hpoapi.io.HPOParser;

public class HPOParserTest {

    private static String hpoSmallOboFilePath=null;

    @Before
    public void setup() {
        ClassLoader classLoader = HPOParserTest.class.getClassLoader();
        hpoSmallOboFilePath=classLoader.getResource("hp-smalltest.obo").getFile();
    }

    @Test public void testInputOntology() {
        HPOParser parser = new HPOParser(hpoSmallOboFilePath);
        Assert.assertNotNull(parser);
    }

}
