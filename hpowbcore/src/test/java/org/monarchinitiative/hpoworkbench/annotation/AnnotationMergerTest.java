package org.monarchinitiative.hpoworkbench.annotation;

import com.google.common.collect.ImmutableList;
import org.junit.BeforeClass;
import org.junit.Test;
import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.hpoworkbench.io.HPOParserTest;
import org.monarchinitiative.phenol.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests mergin of some diseases with no metadata for the terms.
 * Osteomesopyknosis_ORPHANET:ORPHA:2777
 * <ol>
 *     <li>HP:0002808-Kyphosis</li>
 *     <li>HP:0002650-Scoliosis</li>
 *     <li>HP:0003103-Abnormal cortical bone morphology</li>
 *     <li>HP:0011001-Increased bone mineral density</li>
 *     <li>HP:0003312-Abnormal form of the vertebral bodies</li>
 *     <li>HP:0100861-Vertebral body sclerosis</li>
 * </ol>
 * OMIM:OSTEOMESOPYKNOSIS (OMIM:166450)
 * <ol>
 *     <li>HP:0000006-Autosomal dominant inheritance</li>
 *     <li>HP:0000789-Infertility</li>
 *     <li>HP:0011001-Increased bone mineral density</li>
 *     <li>HP:0003419-Low back pain</li>
 * </ol>
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>

 */
public class AnnotationMergerTest {

    private static HpoDisease Osteomesopyknosis_ORPHANET;
    private static HpoDisease OSTEOMESOPYKNOSIS_OMIM;
    private static HpoDisease CCAIDCMS_ORPHA;
    private static HpoDisease CCAIDCMS_OMIM;
    private static HpoOntology ontology;

    @BeforeClass
    public static void init() {
        // Input HPO ontology object
        ClassLoader classLoader = AnnotationMergerTest.class.getClassLoader();
        String hpoPath=classLoader.getResource("hp.obo").getFile();
        HPOParser parser = new HPOParser(hpoPath);
        ontology = parser.getHPO();
        // Make an ORPHANET disease
        List<HpoAnnotation> orpha = ImmutableList.of(new HpoAnnotation("HP:0002808"),
                new HpoAnnotation("HP:0002650"),
                new HpoAnnotation("HP:0003103"),
                new HpoAnnotation("HP:0011001"),
                new HpoAnnotation("HP:0003312"),
                new HpoAnnotation("HP:0100861")
                );
        Osteomesopyknosis_ORPHANET = makeHpoDisease("Osteomesopyknosis","ORPHA","2777",orpha);
        // Make an OMIM disease
        List<HpoAnnotation> omim = ImmutableList.of(new HpoAnnotation("HP:0000006"),
                new HpoAnnotation("HP:0000789"),
                new HpoAnnotation("HP:0011001"),
                new HpoAnnotation("HP:0003419")
        );
        OSTEOMESOPYKNOSIS_OMIM = makeHpoDisease("OSTEOMESOPYKNOSIS","OMIM","166450",omim);
        setupCCAIDCMS();

    }

    /**
     * Make two disease object with lots of annotations
     * Abbreviate CCAIDCMS
     * Corpus callosum agenesis-intellectual disability-coloboma-micrognathia syndrome ORPHA:52055

     */
    private static void setupCCAIDCMS(){
        List<HpoAnnotation> orpha = new ArrayList<>();
        //orpha.add(new HpoAnnotation("HP: "));
        orpha.add(new HpoAnnotation("HP:0000426"));
        orpha.add(new HpoAnnotation("HP:0000175"));
        orpha.add(new HpoAnnotation("HP:0000612"));
        orpha.add(new HpoAnnotation("HP:0000494"));
        orpha.add(new HpoAnnotation("HP:0000218"));
        orpha.add(new HpoAnnotation("HP:0000348"));
        orpha.add(new HpoAnnotation("HP:0000453"));
        orpha.add(new HpoAnnotation("HP:0000588"));
        orpha.add(new HpoAnnotation("HP:0001629"));
        orpha.add(new HpoAnnotation("HP:0001643"));
        orpha.add(new HpoAnnotation("HP:0000407"));
        orpha.add(new HpoAnnotation("HP:0000369"));
        orpha.add(new HpoAnnotation("HP:0000378"));
        orpha.add(new HpoAnnotation("HP:0000639"));
        orpha.add(new HpoAnnotation("HP:0001274"));
        orpha.add(new HpoAnnotation("HP:0001249"));
        orpha.add(new HpoAnnotation("HP:0000767"));
        orpha.add(new HpoAnnotation("HP:0000278"));
        orpha.add(new HpoAnnotation("HP:0002650"));
        orpha.add(new HpoAnnotation("HP:0000256"));
        orpha.add(new HpoAnnotation("HP:0000470"));
        orpha.add(new HpoAnnotation("HP:0004322"));
        CCAIDCMS_ORPHA=makeHpoDisease("CCAIDCMS","ORHPA","52055",orpha);

        List<HpoAnnotation> mim = new ArrayList<>();
        mim.add(new HpoAnnotation("HP:0001419"));
        mim.add(new HpoAnnotation("HP:0000612"));
        mim.add(new HpoAnnotation("HP:0000494"));
        mim.add(new HpoAnnotation("HP:0000218"));
        mim.add(new HpoAnnotation("HP:0000475"));
        mim.add(new HpoAnnotation("HP:0000348"));
        mim.add(new HpoAnnotation("HP:0000588"));
        mim.add(new HpoAnnotation("HP:0000407"));
        mim.add(new HpoAnnotation("HP:0000369"));
        mim.add(new HpoAnnotation("HP:0000378"));
        mim.add(new HpoAnnotation("HP:0000505"));
        mim.add(new HpoAnnotation("HP:0001274"));
        mim.add(new HpoAnnotation("HP:0001249"));
        mim.add(new HpoAnnotation("HP:0000767"));
        mim.add(new HpoAnnotation("HP:0000278"));
        mim.add(new HpoAnnotation("HP:0002650"));
        mim.add(new HpoAnnotation("HP:0000256"));
        mim.add(new HpoAnnotation("HP:0000470"));
        mim.add(new HpoAnnotation("HP:0004322"));
        CCAIDCMS_OMIM = makeHpoDisease("CCAIDCMS","OMIM","300472",mim);


    }


    private static HpoDisease makeHpoDisease(String diseaseName, String database, String diseaseId, List<HpoAnnotation> annots) {
        List<TermId> emptyList = ImmutableList.of();
        return
                new HpoDisease(
                        diseaseName,
                        database,
                        diseaseId,
                        annots,
                        emptyList,
                        emptyList);
    }

    @Test
    public void testMergeOsteomesopyknosis() {
        assertNotNull(OSTEOMESOPYKNOSIS_OMIM);
        AnnotationMerger merger = new AnnotationMerger(OSTEOMESOPYKNOSIS_OMIM,Osteomesopyknosis_ORPHANET,ontology);
        merger.merge();
        assertNotNull(OSTEOMESOPYKNOSIS_OMIM);
    }


    @Test
    public void testMergeCCAIDCMS() {
        assertNotNull(CCAIDCMS_OMIM);
        AnnotationMerger merger = new AnnotationMerger(CCAIDCMS_OMIM,CCAIDCMS_ORPHA,ontology);
        merger.merge();
    }


}
