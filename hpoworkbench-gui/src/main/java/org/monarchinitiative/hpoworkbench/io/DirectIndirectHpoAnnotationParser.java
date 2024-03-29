package org.monarchinitiative.hpoworkbench.io;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.hpoworkbench.exception.HPOWorkbenchException;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseaseAnnotation;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseases;
import org.monarchinitiative.phenol.annotations.io.hpo.*;
import org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * The purpose of this class is to parse the phenotype.hpoa file in order to give the user
 * and overview of the diseases annotated to any given HPO term. The {@link #directAnnotationMap} and
 * {@link #totalAnnotationMap} are used to generate the displays for each disease annotated with HPO terms.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.2.14
 */
public class DirectIndirectHpoAnnotationParser {
    private static final Logger logger = LoggerFactory.getLogger(DirectIndirectHpoAnnotationParser.class);
    /** Path to phenotyoe.hpoa */
    private final String pathToPhenotypeAnnotationTab;
    /** Reference to HPO ontology object. */
    private final Ontology ontology;
    /** Key: term id of an HPO term; value: List of references to diseases directly annotated to his term */
    private Map<TermId,List<HpoDisease>> directAnnotationMap;
    /** Key: term id of an HPO term; value: List of references to diseases directly or indirectly annotated to his term */
    private Map<TermId,List<HpoDisease>> totalAnnotationMap;

    /**
     * @param path Path to phenotype.hpoa
     * @param onto reference to HPO Ontology
     */
    public DirectIndirectHpoAnnotationParser(String path, Ontology onto) {
        this.pathToPhenotypeAnnotationTab = path;
        this.ontology = onto;
        try {
            doParse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Get map with direct annotations. The map will be <code>null</code>, if the {@link #doParse()} method have not been
     * invoked.
     *
     * @return {@link Map} mapping {@link TermId}s to {@link List} of their {@link HpoDisease}s
     */
    public Map<TermId, List<HpoDisease>> getDirectAnnotMap() {
        return directAnnotationMap;
    }

    /**
     * Get map with indirect annotations. The map will be <code>null</code>, if the {@link #doParse()} method have not been
     * invoked.
     *
     * @return {@link Map} mapping {@link TermId}s to {@link List} of their {@link HpoDisease}s
     */
    public Map<TermId, List<HpoDisease>> getTotalAnnotationMap() {
        return totalAnnotationMap;
    }

    /**
     * Parse annotations file and populate maps containing direct and indirect annotations.
     */
    private void doParse() throws HPOWorkbenchException, IOException {
        if (ontology == null) {
            logger.warn("Ontology unset, cannot parse annotations file");
            return;
        }
        logger.trace("doParse in DirectIndirectParser");
        HpoDiseaseLoaderOptions options = HpoDiseaseLoaderOptions.defaultOptions();
        HpoDiseaseLoader loader = HpoDiseaseLoaders.defaultLoader(ontology, options);
        HpoDiseases diseases = loader.load(Path.of(pathToPhenotypeAnnotationTab));
        Map<TermId, HpoDisease> diseaseMap = diseases.diseaseById();
        directAnnotationMap=new HashMap<>();
        totalAnnotationMap=new HashMap<>();
        Map<TermId, Set<HpoDisease>> tempmap = new HashMap<>();
        if (diseaseMap==null) {
            throw new HPOWorkbenchException("disease map was null after parse of "+pathToPhenotypeAnnotationTab);
        }
        for (TermId diseaseId : diseaseMap.keySet()) {
            HpoDisease disease = diseaseMap.get(diseaseId);
            for  (HpoDiseaseAnnotation annot : disease.annotations()) {
                TermId hpoId = annot.id();

                Set<TermId> ancs = new HashSet<>();
                if (ontology.containsTerm(hpoId)) {
                    hpoId = ontology.getPrimaryTermId(hpoId);
                    ancs = OntologyAlgorithm.getAncestorTerms(ontology, hpoId, true);
                    directAnnotationMap.putIfAbsent(hpoId,new ArrayList<>());
                    directAnnotationMap.get(hpoId).add(disease);
                } else {
                    logger.error("Term {} in disease {} not contained in ontology", hpoId.getValue(), disease.diseaseName());
                }
                for (TermId t : ancs) {
                    tempmap.putIfAbsent(t, new HashSet<>());
                    Set<HpoDisease> diseaseset = tempmap.get(t);
                    diseaseset.add(disease);
                }
            }
            // Also add the modes of inheritance to the annotations
            for (TermId inheritanceId : disease.modesOfInheritance()) {
                directAnnotationMap.putIfAbsent(inheritanceId,new ArrayList<>());
                directAnnotationMap.get(inheritanceId).add(disease);
                Set<TermId> ancs = OntologyAlgorithm.getAncestorTerms(ontology, inheritanceId, true);
                for (TermId t : ancs) {
                    tempmap.putIfAbsent(t, new HashSet<>());
                    Set<HpoDisease> diseaseset = tempmap.get(t);
                    diseaseset.add(disease);
                }
            }
        }
        // When we get here, we transform the sets into an immutable, sorted list
        ImmutableMap.Builder<TermId, List<HpoDisease>> mapbuilder = new ImmutableMap.Builder<>();
        for (TermId key : tempmap.keySet()) {
            ImmutableList.Builder<HpoDisease> listbuilder = new ImmutableList.Builder<>();
            listbuilder.addAll(tempmap.get(key));
            mapbuilder.put(key, listbuilder.build());
        }
        this.totalAnnotationMap = mapbuilder.build();
    }

}
