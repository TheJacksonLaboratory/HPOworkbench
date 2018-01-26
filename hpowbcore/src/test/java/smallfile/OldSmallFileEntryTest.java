package smallfile;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.formats.hpo.HpoTermRelation;
import com.github.phenomics.ontolib.ontology.data.Ontology;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.monarchinitiative.hpoworkbench.io.HpoOntologyParser;
import org.monarchinitiative.hpoworkbench.smallfile.OldSmallFile;
import org.monarchinitiative.hpoworkbench.smallfile.OldSmallFileEntry;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.monarchinitiative.hpoworkbench.util.DateUtil.convertToCanonicalDateFormat;

public class OldSmallFileEntryTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
/*
 private HpoOntology ontology=null;
    private Ontology<HpoTerm, HpoTermRelation> inheritanceSubontology=null;
    private Ontology<HpoTerm, HpoTermRelation> abnormalPhenoSubOntology=null;
 */
    @BeforeClass
    public static void init() {
        ClassLoader classLoader = OldSmallFileEntryTest.class.getClassLoader();
        String hpOboPath = classLoader.getResource("hp.obo").getFile();
        try {
            HpoOntologyParser parser = new HpoOntologyParser(hpOboPath);
            HpoOntology ontology = parser.getOntology();
            Ontology<HpoTerm, HpoTermRelation> inheritanceSubontology = parser.getInheritanceSubontology();
            Ontology<HpoTerm, HpoTermRelation> abnormalPhenoSubOntology = parser.getPhenotypeSubontology();
            OldSmallFileEntry.setOntology(ontology, inheritanceSubontology, abnormalPhenoSubOntology);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



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
            bw.write(annot + "\n");
        }
        bw.close();
    }

    private void printTempAnnotationFileToShell(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        System.out.println("#############  Annotation file ###############");
        System.out.println(path);
        while ((line=br.readLine())!=null) {
            System.out.println("\""+line+"\"");
        }
        br.close();
        System.out.println("#############  End ###############");
    }



    @Test
    public void testDiseaseName() throws IOException {
        init();
        File tempFile = testFolder.newFile("tempfile.tab");
        List<String> annots=new ArrayList<>();
        annots.add(SmallFileBuilder.getHeader());
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:220220").
                diseaseName("220220 DANDY-WALKER MALFORMATION WITH POSTAXIAL POLYDACTYLY;;DWM WITH POSTAXIAL POLYDACTYLY;;PIERQUIN SYNDROME");
        //System.out.println("BB" + builder.build());
        annots.add(builder.build());
        writeTmpFile(annots,tempFile);
        //printTempAnnotationFileToShell(tempFile.getAbsolutePath());
        OldSmallFile osm = new OldSmallFile(tempFile.getAbsolutePath());
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        assertEquals(1,entries.size());


    }



}
