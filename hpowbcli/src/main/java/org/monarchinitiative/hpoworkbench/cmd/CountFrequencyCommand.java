package org.monarchinitiative.hpoworkbench.cmd;


import com.github.phenomics.ontolib.formats.hpo.HpoDiseaseAnnotation;
import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.graph.data.Edge;
import com.github.phenomics.ontolib.ontology.data.*;
import org.apache.log4j.Logger;
import org.monarchinitiative.hpoworkbench.exception.HPOException;
import org.monarchinitiative.hpoworkbench.io.HPOAnnotationParser;
import org.monarchinitiative.hpoworkbench.io.HpoOntologyParser;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The situation is that we have a list of disease annotations (which could be {@code phenotype_annotation.tab} or
 * a smaller selection of annotations) and an HPO term. We would like to find out the total number of annotations
 * to the term or any of its ancestors. This command will outpout a list of these counts to the shell.
 * @author <a href="mailto:peter.robinson">Peter Robinson</a>
 */
public class CountFrequencyCommand extends HPOCommand {
    private static Logger LOGGER = Logger.getLogger(DownloadCommand.class.getName());

    private final String hpOboPath;

    private final String annotationPath;

    private final TermId termId;

    private int descendentTermCount;
    /** County of annotations to any descendent of {@link #termId}. */
    private int totalAnnotationCount=0;

    private int TERMS_TO_SHOW=10;

    public CountFrequencyCommand(String hpoPath, String annotPath, String hpoTermId) {
        this.hpOboPath=hpoPath;
        this.annotationPath=annotPath;

        if (hpoTermId.startsWith("HP:")) {
            hpoTermId=hpoTermId.substring(3);
        }
        TermPrefix HP_PREFIX = new ImmutableTermPrefix("HP");
        termId = new ImmutableTermId(HP_PREFIX,hpoTermId);
    }

    public void run() {
        try {
            HpoOntologyParser oparser = new HpoOntologyParser(hpOboPath);
            HpoOntology ontology = oparser.getOntology();
            HPOAnnotationParser aparser = new HPOAnnotationParser(annotationPath);
            List<HpoDiseaseAnnotation> annotlist = aparser.getAnnotations();
            Set<TermId> descendents = getDescendents(ontology, termId);
            descendentTermCount = descendents.size();
            HashMap<TermId, Double> hm = new HashMap<>();
            for (TermId t : descendents) {
                hm.put(t, 0D);
            }
            for (HpoDiseaseAnnotation annot : annotlist) {
                TermId hpoid = annot.getHpoId();
                double freq = annot.getFrequency().orElse(0.0F);
                if (descendents.contains(hpoid)) {
                    hm.put(termId, freq + hm.get(termId));
                    totalAnnotationCount++;
                }
            }
            outputCounts(hm, ontology);
        } catch (HPOException e) {
            e.printStackTrace();
            LOGGER.error("Could not input ontology: {}",e);
            System.exit(1);
        }


    }

    /**
     * Sort a map by values and return a sorted map with the top {@link #TERMS_TO_SHOW} items.
     * @param map Here, keys are terms and values are disease annotations
     * @param <K> key
     * @param <V> value
     * @return sorted map with top TERMS_TO_SHOW entries
     */
    private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .limit(TERMS_TO_SHOW)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private void outputCounts(HashMap<TermId,Double> hm, Ontology ontology) {
        Map mp2 = sortByValue(hm);
        String termS=String.format("%s [%s]",((HpoTerm)ontology.getTermMap().get(termId)).getName(),termId.getIdWithPrefix());
        System.out.println();
        System.out.println("Annotation counts for " + termS);
        System.out.println("\tNumber of descendent terms: " + descendentTermCount);
        System.out.print(String.format("\tTotal annotations to any descendent of %s: %d ",termS, totalAnnotationCount));
        System.out.println();

        for (Object t: mp2.keySet()) {
            TermId tid = (TermId) t;
            double count = (double)mp2.get(t);
            String name =  ((HpoTerm)ontology.getTermMap().get(tid)).getName();
            System.out.println(name + " [" +tid.getIdWithPrefix() + "]: " + count);
        }
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


    public String getName() {
        return "count-frequency";
    }

}
