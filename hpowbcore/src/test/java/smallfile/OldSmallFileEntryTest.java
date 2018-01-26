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
import org.monarchinitiative.hpoworkbench.smallfile.V2SmallFile;
import org.monarchinitiative.hpoworkbench.smallfile.V2SmallFileEntry;

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
        String olddate = "2012.04.11";
        String expected = "2012-04-11";
        assertEquals(expected, convertToCanonicalDateFormat(olddate));
    }

    @Test
    public void testDateCorrection3() {
        String olddate = "2009.02.17";
        String expected = "2009-02-17";
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
        while ((line = br.readLine()) != null) {
            System.out.println("\"" + line + "\"");
        }
        br.close();
        System.out.println("#############  End ###############");
    }


    @Test
    public void testDiseaseName() throws IOException {
        File tempFile = testFolder.newFile("tempfile.tab");
        List<String> annots = new ArrayList<>();
        annots.add(SmallFileBuilder.getHeader());
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:220220").
                diseaseName("220220 DANDY-WALKER MALFORMATION WITH POSTAXIAL POLYDACTYLY;;DWM WITH POSTAXIAL POLYDACTYLY;;PIERQUIN SYNDROME");
        //System.out.println("BB" + builder.build());
        annots.add(builder.build());
        writeTmpFile(annots, tempFile);
        //printTempAnnotationFileToShell(tempFile.getAbsolutePath());
        OldSmallFile osm = new OldSmallFile(tempFile.getAbsolutePath());
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        assertEquals(1, entries.size());
    }


    /* This is the modification field. In this case, we want to add the HPO Modifier "Episodic" (HP:0025303).
    MODIFIER:EPISODIC;OMIM-CS:NEUROLOGIC_CENTRAL NERVOUS SYSTEM > QUADRIPLEGIA, EPISODIC
    This comes from the line
    OMIM-104290.tab:OMIM:104290	#104290 ALTERNATING HEMIPLEGIA OF CHILDHOOD 1; AHC1					HP:0002445	Tetraplegia			IEA	IEA						MODIFIER:EPISODIC;OMIM-CS:NEUROLOGIC_CENTRAL NERVOUS SYSTEM > QUADRIPLEGIA, EPISODIC	OMIM:104290	HPO:skoehler	06.06.2013

    */
    @Test
    public void testModification() throws IOException {
        File tempFile = testFolder.newFile("tempfile2.tab");
        List<String> annots = new ArrayList<>();
        annots.add(SmallFileBuilder.getHeader());
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:104290").
                diseaseName("#104290 ALTERNATING HEMIPLEGIA OF CHILDHOOD 1; AHC1").
                hpoId("HP:0002445").
                hpoName("Tetraplegia").
                description("MODIFIER:EPISODIC;OMIM-CS:NEUROLOGIC_CENTRAL NERVOUS SYSTEM > QUADRIPLEGIA, EPISODIC");
        String oldSmallFileLine = builder.build();
        annots.add(oldSmallFileLine);
        writeTmpFile(annots, tempFile);
        OldSmallFile osm = new OldSmallFile(tempFile.getAbsolutePath());
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        // we expect one entry, and the Modifer field should have "Episodic" (HP:0025303)
        assertEquals(1, entries.size());
        OldSmallFileEntry entry = entries.get(0);
        assertEquals("OMIM:104290", entry.getDiseaseID());
        assertEquals("#104290 ALTERNATING HEMIPLEGIA OF CHILDHOOD 1; AHC1", entry.getDiseaseName());
        assertEquals("HP:0002445", entry.getPhenotypeId().getIdWithPrefix());
        assertEquals("Tetraplegia", entry.getPhenotypeName());
        assertEquals("IEA", entry.getEvidenceID());
        assertEquals("HP:0025303", entry.getModifierString());
        assertEquals("OMIM-CS:NEUROLOGIC_CENTRAL NERVOUS SYSTEM > QUADRIPLEGIA, EPISODIC", entry.getDescription());

//        System.out.println("### OLD SMALL FILE  ###");
//        System.out.println(oldSmallFileLine);
//        System.out.println("### NEW SMALL FILE  ###");
//        V2SmallFileEntry v2e=new V2SmallFileEntry(entry);
//        System.out.println(v2e.getRow());
    }


    /* This entry has the free test "Mild" in the  description field. This should be transfered to the Modifier field
    OMIM:614255	#614255 MENTAL RETARDATION, AUTOSOMAL DOMINANT 9; MRD9				KIF1A	HP:0006855	Cerebellar vermis atrophy			IEA	IEA	1/1					MildOMIM:614255	HPO:probinson	Aug 10, 2013
    */
    @Test
    public void testModification2() throws IOException {
        File tempFile = testFolder.newFile("tempfile2.tab");
        List<String> annots = new ArrayList<>();
        annots.add(SmallFileBuilder.getHeader());
        SmallFileBuilder builder = new SmallFileBuilder().
                diseaseId("OMIM:614255").
                diseaseName("#614255 MENTAL RETARDATION, AUTOSOMAL DOMINANT 9; MRD9").
                hpoId("HP:0006855").
                hpoName("Cerebellar vermis atrophy").
                description("Mild");
        String oldSmallFileLine = builder.build();
        annots.add(oldSmallFileLine);
        writeTmpFile(annots, tempFile);
        OldSmallFile osm = new OldSmallFile(tempFile.getAbsolutePath());
        List<OldSmallFileEntry> entries = osm.getEntrylist();
        // we expect one entry, and the Modifer field should have "Episodic" (HP:0025303)
        assertEquals(1, entries.size());
        OldSmallFileEntry entry = entries.get(0);
        assertEquals("OMIM:614255", entry.getDiseaseID());
        assertEquals("#614255 MENTAL RETARDATION, AUTOSOMAL DOMINANT 9; MRD9", entry.getDiseaseName());
        assertEquals("HP:0006855", entry.getPhenotypeId().getIdWithPrefix());
        assertEquals("Cerebellar vermis atrophy", entry.getPhenotypeName());
        assertEquals("IEA", entry.getEvidenceID());
        assertEquals("HP:0012825", entry.getModifierString()); // code for Mild
        assertEquals("", entry.getDescription());

    }

}