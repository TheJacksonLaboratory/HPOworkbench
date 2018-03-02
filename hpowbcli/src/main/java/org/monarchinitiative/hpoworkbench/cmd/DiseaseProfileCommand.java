package org.monarchinitiative.hpoworkbench.cmd;

import com.github.phenomics.ontolib.formats.hpo.*;
import com.github.phenomics.ontolib.io.obo.hpo.HpoAnnotation2DiseaseParser;
import com.github.phenomics.ontolib.ontology.data.*;
import org.apache.log4j.Logger;
import org.monarchinitiative.hpoworkbench.annotation.DiseaseProfile;
import org.monarchinitiative.hpoworkbench.exception.HPOException;
import org.monarchinitiative.hpoworkbench.io.HpoOntologyParser;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DiseaseProfileCommand extends HPOCommand {
    private static Logger LOGGER = Logger.getLogger(Hpo2HpoCommand.class.getName());
    private final String hpOboPath;

    private final String annotationPath;

    /** All of the ancestor terms of . */
    private Set<TermId> descendents=null;
    /** Annotations of all of the diseases in the HPO corpus. */
    private List<HpoDiseaseAnnotation> annotlist=null;

    private HpoOntology ontology=null;
    private Ontology<HpoTerm, HpoTermRelation > phenotypeOntology;
    private Ontology<HpoTerm, HpoTermRelation> inheritanceOntology;

    private Map<String,HpoDiseaseWithMetadata> diseasemap=null;

    private HpoDiseaseWithMetadata diseaseWithMetadata=null;

    private final String diseaseString;


    public DiseaseProfileCommand(String hpoPath, String annotPath, String disease) {
        this.hpOboPath = hpoPath;
        this.annotationPath = annotPath;
        diseaseString = disease;

    }









    public void run() {
        LOGGER.trace("Running profile command...");
        inputHpoData();
        inputHpoDiseaseAnnotations();
        HpoDiseaseWithMetadata disease = diseasemap.get(diseaseString);
        if (disease == null) {
            LOGGER.error("Could not find disease object for " + diseaseString);
            LOGGER.error("Sorry, but we will terminate....");

            System.exit(1);
        }
        DiseaseProfile profile = new DiseaseProfile(ontology,disease);
        profile.dumpProfileToShell();
//        for (String name : diseasemap.keySet()) {
//            System.out.println(name);
//        }
    }


    /** input the hp.obo and the annotations. */
    private void inputHpoData() {
        try {
            HpoOntologyParser oparser = new HpoOntologyParser(hpOboPath);
            this.ontology = oparser.getOntology();
            inheritanceOntology = oparser.getInheritanceSubontology();
            phenotypeOntology = oparser.getPhenotypeSubontology();
        } catch (HPOException e) {
            LOGGER.error(String.format("Could not input ontology: %s",e.getMessage()));
            System.exit(1);
        }
    }


    private void inputHpoDiseaseAnnotations() {
        HpoAnnotation2DiseaseParser parser = new HpoAnnotation2DiseaseParser(annotationPath,
                phenotypeOntology,inheritanceOntology);
        this.diseasemap = parser.getDiseaseMap();

    }


    public String getName(){return "disease-profile";}
}
