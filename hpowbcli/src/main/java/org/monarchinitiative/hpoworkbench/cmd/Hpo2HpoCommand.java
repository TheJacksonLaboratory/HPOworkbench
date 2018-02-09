package org.monarchinitiative.hpoworkbench.cmd;

import com.github.phenomics.ontolib.formats.hpo.*;
import com.github.phenomics.ontolib.graph.data.Edge;
import com.github.phenomics.ontolib.io.obo.hpo.HpoAnnotation2DiseaseParser;
import com.github.phenomics.ontolib.ontology.data.*;
import org.apache.log4j.Logger;
import org.monarchinitiative.hpoworkbench.annotation.Hpo2Hpo;
import org.monarchinitiative.hpoworkbench.exception.HPOException;
import org.monarchinitiative.hpoworkbench.io.HPOAnnotationParser;
import org.monarchinitiative.hpoworkbench.io.HpoOntologyParser;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class drives the HPO term "cross-correlation" analysis.
 */
public class Hpo2HpoCommand extends HPOCommand {
    private static Logger LOGGER = Logger.getLogger(Hpo2HpoCommand.class.getName());
    private final String hpOboPath;

    private final String annotationPath;

    private final TermId termId;
    /** All of the ancestor terms of {@link #termId}. */
    private Set<TermId> descendents=null;
    /** Annotations of all of the diseases in the HPO corpus. */
    private List<HpoDiseaseAnnotation> annotlist=null;

    private HpoOntology ontology=null;
    private  Ontology<HpoTerm, HpoTermRelation > phenotypeOntology;
    private Ontology<HpoTerm, HpoTermRelation> inheritanceOntology;

    private Map<String,HpoDiseaseWithMetadata> diseasemap=null;


    public Hpo2HpoCommand(String hpoPath, String annotPath, String hpoTermId) {
        this.hpOboPath = hpoPath;
        this.annotationPath = annotPath;

        if (hpoTermId.startsWith("HP:")) {
            hpoTermId = hpoTermId.substring(3);
        }
        TermPrefix HP_PREFIX = new ImmutableTermPrefix("HP");
        termId = new ImmutableTermId(HP_PREFIX, hpoTermId);
    }



        /**
         * Function for the execution of the command.
         *
         */
    @Override public  void run() {
        inputHpoData();
        inputHpoDiseaseAnnotations();
        Hpo2Hpo h2h = new Hpo2Hpo(termId, ontology,diseasemap);
        h2h.calculateHpo2Hpo();
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




    @Override public String getName() { return "hpo2hpo"; }
}
