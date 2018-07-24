package org.monarchinitiative.hpoworkbench.io;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.model.DiseaseModel;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermPrefix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * The purpose of this class is to parse the phenotype_annotation.tab file in order to give the user
 * and overview of the diseases annotated to any given HPO term.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.2.13
 */
public class DirectIndirectHpoAnnotationParser {

    private static final Logger logger = LogManager.getLogger();

    private final String pathToPhenotypeAnnotationTab;

    private final HpoOntology ontology;

    private final TermPrefix HP_PREFIX = new TermPrefix("HP");


    private Map<TermId, List<DiseaseModel>> directAnnotMap = null;


    private Map<TermId, List<DiseaseModel>> indirectAnnotMap = null;

    public DirectIndirectHpoAnnotationParser(String path, HpoOntology onto) {
        this.pathToPhenotypeAnnotationTab = path;
        this.ontology = onto;
    }


    /**
     * Get map with direct annotations. The map will be <code>null</code>, if the {@link #doParse()} method have not been
     * invoked.
     *
     * @return {@link Map} mapping {@link TermId}s to {@link List} of their {@link DiseaseModel}s
     */
    public Map<TermId, List<DiseaseModel>> getDirectAnnotMap() {
        return directAnnotMap;
    }

    /**
     * Get map with indirect annotations. The map will be <code>null</code>, if the {@link #doParse()} method have not been
     * invoked.
     *
     * @return {@link Map} mapping {@link TermId}s to {@link List} of their {@link DiseaseModel}s
     */
    public Map<TermId, List<DiseaseModel>> getIndirectAnnotMap() {
        return indirectAnnotMap;
    }

    private TermId string2TermId(String termstring) {
        if (termstring.startsWith("HP:")) {
            termstring = termstring.substring(3);
        }
        if (termstring.length() != 7) {
            logger.error("Malformed termstring: " + termstring);
            return null;
        }
        TermId tid = new TermId(HP_PREFIX, termstring);
        if (!ontology.getAllTermIds().contains(tid)) {
            logger.error("Unknown TermId " + tid.getIdWithPrefix());
            return null;
        }
        return tid;
    }

    /**
     * Parse annotations file and populate maps containing direct and indirect annotations.
     */
    public void doParse() {
        if (ontology == null) {
            logger.warn("Ontology unset, cannot parse annotations file");
            return;
        }
        logger.trace("doParse in DirectIndirectParser");
        Map<TermId, HpoDisease> diseaseMap=null;
        HpoDiseaseAnnotationParser daparser = new HpoDiseaseAnnotationParser(this.pathToPhenotypeAnnotationTab,this.ontology);
        try {
            diseaseMap = daparser.parse();
            logger.trace("Got " + diseaseMap.size() + " disease models");
        } catch (PhenolException pe) {
            pe.printStackTrace();
        }

        indirectAnnotMap = new HashMap<>();
        directAnnotMap = new HashMap<>();
        Map<TermId, Set<DiseaseModel>> tempmap = new HashMap<>();

        for (TermId diseaseId : diseaseMap.keySet()) {
            HpoDisease dis = diseaseMap.get(diseaseId);
            String db = dis.getDatabase();
            String diseaseName = dis.getName();
            DiseaseModel diseaseModel = new DiseaseModel(db,diseaseId.getId(),diseaseName);
            for (HpoAnnotation annot : dis.getPhenotypicAbnormalities()) {
                TermId hpoId = annot.getTermId();
                directAnnotMap.putIfAbsent(hpoId, new ArrayList<>());
                directAnnotMap.get(hpoId).add(diseaseModel);
                Set<TermId> ancs = OntologyAlgorithm.getAncestorTerms(ontology, hpoId, true);
                for (TermId t : ancs) {
                    tempmap.putIfAbsent(t, new HashSet<>());
                    Set<DiseaseModel> diseaseset = tempmap.get(t);
                    diseaseset.add(diseaseModel);
                }
            }
        }
        // When we get here, we transform the sets into an immutable, sorted list
        ImmutableMap.Builder<TermId, List<DiseaseModel>> mapbuilder = new ImmutableMap.Builder<>();
        for (TermId key : tempmap.keySet()) {
            ImmutableList.Builder<DiseaseModel> listbuilder = new ImmutableList.Builder<>();
            listbuilder.addAll(tempmap.get(key));
            mapbuilder.put(key, listbuilder.build());
        }
        this.indirectAnnotMap = mapbuilder.build();
    }

}
