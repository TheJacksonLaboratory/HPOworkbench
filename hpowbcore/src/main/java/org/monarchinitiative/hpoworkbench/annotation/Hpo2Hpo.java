package org.monarchinitiative.hpoworkbench.annotation;

import com.github.phenomics.ontolib.formats.hpo.*;
import com.github.phenomics.ontolib.graph.data.Edge;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermPrefix;
import com.github.phenomics.ontolib.ontology.data.Ontology;
import com.github.phenomics.ontolib.ontology.data.TermId;
import com.github.phenomics.ontolib.ontology.data.TermPrefix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class Hpo2Hpo {
    private static final Logger logger = LogManager.getLogger();

    private final TermId hpoTermId;
    /**
     * All of the ancestor terms of {@link #hpoTermId}.
     */
    private Set<TermId> descendents = null;
    /**
     * Diseases annotated to {@link #hpoTermId}.
     */
    private final Map<String, HpoDiseaseWithMetadata> diseasemap;

    private static TermPrefix HP_PREFIX = new ImmutableTermPrefix("HP");
    /**
     * Names of diseases annotated to the {@link #hpoTermId} or its descendents.
     */
    private Set<String> diseasesAnnotatedToHpoTerm;


    private final HpoOntology ontology;

    public Hpo2Hpo(TermId termId, HpoOntology onto, Map<String, HpoDiseaseWithMetadata> dmap) {
        this.ontology = onto;
        TermId current = ontology.getTermMap().get(termId).getId();
        if (! current.equals(termId)) {
            logger.trace("[WARNING] Replacing alt_id with current HPO Term id" + current.getIdWithPrefix());
        }
        this.hpoTermId = current;
        this.diseasemap = dmap;
        diseasesAnnotatedToHpoTerm = new HashSet<>();
    }

    public void calculateHpo2Hpo() {
        HpoTerm term = ontology.getTermMap().get(hpoTermId);
        if (term==null) {
            logger.error("We could not find a term for id " + hpoTermId.getIdWithPrefix());
            logger.error("Terminating program....");
        } else {
            logger.trace(String.format("We will analyze term %s [%s]",term.getName(),hpoTermId.getIdWithPrefix()));
        }
        // Note-- to prevent errors, make sure we do not have an alt id.
        TermId currentId = term.getId();
        if (! hpoTermId.equals(currentId)) {
            logger.trace("replacing alt id with current id "+ currentId.getIdWithPrefix());
        }


        this.descendents = getDescendents(ontology, currentId);
        findAnnotatedDiseases();
        outputCategoryHpo2Hpo();

    }


    private void outputCategoryHpo2Hpo() {
        Map<TermId,Integer> termCountMap = new HashMap<>();
        HpoCategoryMap hpocatmap = new HpoCategoryMap();
        for (String disease : diseasesAnnotatedToHpoTerm) {
            HpoDiseaseWithMetadata hdwm = diseasemap.get(disease);
            for (TermIdWithMetadata tiwm : hdwm.getPhenotypicAbnormalities()) {
                hpocatmap.addAnnotatedTerm(tiwm.getTermId(), ontology);
                if (! termCountMap.containsKey(tiwm.getTermId())) {
                    termCountMap.put(tiwm.getTermId(),0);
                }
                // now increment count
                termCountMap.put(tiwm.getTermId(),1+termCountMap.get(tiwm.getTermId()));
            }
        }

        // Get the HpoCategory that corresponds to the term of interest
        // Note can be null -- todo, add exception
       HpoCategory myCategory = hpocatmap.getCategoryForTerm(hpoTermId,ontology);
        List<TermId> bestTermsInMyCategory=new ArrayList<>();
        List<TermId> bestTermsInOtherCategories=new ArrayList<>();
        int LIMIT = 10;

        Map<TermId, Integer> sortedCountMap = sortByValue(termCountMap,LIMIT);
        for (TermId tid: sortedCountMap.keySet()) {
            tid=getCurrentTermId(tid);
            String label = ontology.getTermMap().get(tid).getName();
            String output = String.format("%s [%s] %d",label,tid.getIdWithPrefix(),sortedCountMap.get(tid));
            if (myCategory.containsTerm(tid)) {
                output=output+" (*)";
            }
            System.out.println(output);
        }

    }


    TermId getCurrentTermId(TermId previousId) {
        return ontology.getTermMap().get(previousId).getId();
    }


    private void findAnnotatedDiseases() {
        descendents.add(hpoTermId);
        for (String name : diseasemap.keySet()) {
            HpoDiseaseWithMetadata hdwm = diseasemap.get(name);
            if (hdwm.isDirectlyAnnotatedToAnyOf(descendents)) {
                diseasesAnnotatedToHpoTerm.add(name);
            }
        }
    }




    /** Sort and get back the first K hits. */
    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, int K) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .limit(K)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }




    /** Get the immediate children of a Term. Do not include the original term in the returned set. */
    private  Set<TermId> getTermChildren(Ontology ontology, TermId id) {
        Set<TermId> kids = new HashSet<>();
        Iterator it =  ontology.getGraph().inEdgeIterator(id);
        while (it.hasNext()) {
            Edge<TermId> edge = (Edge<TermId>) it.next();
            TermId sourceId=edge.getSource();
            kids.add(sourceId);
        }
        return kids;
    }

    private Set<TermId> getDescendents(Ontology ontology, TermId parent) {
        Set<TermId> descset = new HashSet<>();
        Stack<TermId> stack = new Stack<>();
        stack.push(parent);
        while (! stack.empty() ) {
            TermId tid = stack.pop();
            descset.add(tid);
            Set<TermId> directChildrenSet = getTermChildren(ontology,tid);
            directChildrenSet.forEach(t -> stack.push(t));
        }
        return descset;
    }


}
