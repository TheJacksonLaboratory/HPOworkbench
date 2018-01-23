package smallfile;

import org.junit.Test;
import org.monarchinitiative.hpoworkbench.util.DateUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Date;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.monarchinitiative.hpoworkbench.util.DateUtil.convertToCanonicalDateFormat;

public class OldSmallFileEntryTest {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Test
    public void testDateCorrection1() {
        String olddate="2012.04.11";
        String expected="2012-04-11";
        assertEquals(expected, convertToCanonicalDateFormat(olddate));
    }


    @Test
    public void testDateCorrection3() {
        String olddate="2009.02.17";
        String expected="2009-02-17";
        assertEquals(expected, convertToCanonicalDateFormat(olddate));
    }



}
