package org.monarchinitiative.hpoworkbench.io;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.model.DiseaseModel;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.ImmutableTermId;
import org.monarchinitiative.phenol.ontology.data.ImmutableTermPrefix;
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

    private final TermPrefix HP_PREFIX = new ImmutableTermPrefix("HP");


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
        TermId tid = new ImmutableTermId(HP_PREFIX, termstring);
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

        indirectAnnotMap = new HashMap<>();
        directAnnotMap = new HashMap<>();
        Map<TermId, Set<DiseaseModel>> tempmap = new HashMap<>();


        try {
            BufferedReader br = new BufferedReader(new FileReader(pathToPhenotypeAnnotationTab));
            String line;
            int c = 0;
            while ((line = br.readLine()) != null) {
                String A[] = line.split("\t");
                if (A.length < 5) {
                    logger.error(String.format("Annotation line with less than 5 fields, \n%s", line));
                    continue;
                }
                String db = A[0];
                String dbId = A[1];
                String diseasename = A[2];
                int i = diseasename.indexOf(";");
                if (i > 0) diseasename = diseasename.substring(0, i); // other synonyms follow the first ";"
                DiseaseModel dis = new DiseaseModel(db, dbId, diseasename);
                String HPOid = A[4];
                TermId id = string2TermId(HPOid);
                if (!directAnnotMap.containsKey(id)) {
                    directAnnotMap.put(id, new ArrayList<>());

                }
                directAnnotMap.get(id).add(dis);
                Set<TermId> ancs = ontology.getAncestorTermIds(id);
                for (TermId t : ancs) {
                    if (!tempmap.containsKey(t)) {
                        tempmap.put(t, new HashSet<>());
                    }
                    Set<DiseaseModel> diseaseset = tempmap.get(t);
                    diseaseset.add(dis);
                }
                // logger.trace(String.format("Adding disease %s [%s:%s] to term %s",diseasename,db,dbId,id.getIdWithPrefix()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // When we get here, we transform the sets into an immutable, sorted list
        ImmutableMap.Builder<TermId, List<DiseaseModel>> mapbuilder = new ImmutableMap.Builder();
        for (TermId key : tempmap.keySet()) {
            ImmutableList.Builder<DiseaseModel> listbuilder = new ImmutableList.Builder();
            listbuilder.addAll(tempmap.get(key));
            mapbuilder.put(key, listbuilder.build());
        }
        this.indirectAnnotMap = mapbuilder.build();
    }

}
