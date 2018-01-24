package smallfile;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.monarchinitiative.hpoworkbench.smallfile.OldSmallFile;
import org.monarchinitiative.hpoworkbench.smallfile.OldSmallFileEntry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.monarchinitiative.hpoworkbench.util.DateUtil.convertToCanonicalDateFormat;

public class OldSmallFileEntryTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

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

    private void writeTmpFile(List<String> annotations, File f) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        for (String annot : annotations) {
            bw.write(annot + "\t");
        }
        bw.close();
    }



    @Test
    public void testDiseaseName() throws IOException {
        File tempFile = testFolder.newFile("tempfile.tab");
        List<String> annots=new ArrayList<>();
        annots.add(SmallFileBuilder.getHeader());
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:220220").
                diseaseName("220220 DANDY-WALKER MALFORMATION WITH POSTAXIAL POLYDACTYLY;;DWM WITH POSTAXIAL POLYDACTYLY;;PIERQUIN SYNDROME");
        annots.add(builder.build());
        for (String s: annots) {
            System.out.println(s);
        }
        writeTmpFile(annots,tempFile);
        OldSmallFile osm = new OldSmallFile(tempFile.getAbsolutePath());
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        assertEquals(1,entries.size());

    }



}
