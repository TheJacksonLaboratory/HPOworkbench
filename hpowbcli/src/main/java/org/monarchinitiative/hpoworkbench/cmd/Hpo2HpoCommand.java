package org.monarchinitiative.hpoworkbench.cmd;


import org.apache.log4j.Logger;
import org.monarchinitiative.hpoworkbench.io.HPOAnnotationParser;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;
import org.monarchinitiative.phenol.ontology.data.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getChildTerms;

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


    public Hpo2HpoCommand(String hpoPath, String annotPath, String hpoTermId) {
        this.hpOboPath = hpoPath;
        this.annotationPath = annotPath;

        if (hpoTermId.startsWith("HP:")) {
            hpoTermId = hpoTermId.substring(3);
        }
        TermPrefix HP_PREFIX = new TermPrefix("HP");
        termId = new TermId(HP_PREFIX, hpoTermId);
    }



        /**
         * Function for the execution of the command.
         */
    @Override public  void run() {
        inputHpoData();
    }

    /**
     * input the hp.obo and the annotations.
     */
    private void inputHpoData() {

        try {
            HpOboParser oparser = new HpOboParser(new File(hpOboPath));
            /* Annotations of all of the diseases in the HPO corpus. */ /** Annotations of all of the diseases in the HPO corpus. */ //private List<HpoDiseaseAnnotation> annotlist=null;
            HpoOntology ontology = oparser.parse();
            HPOAnnotationParser aparser = new HPOAnnotationParser(annotationPath, ontology);
        } catch (PhenolException | FileNotFoundException pe) {
            pe.printStackTrace(); // todo refactor
        }
        //this.annotlist = aparser.getAnnotations();
        throw new UnsupportedOperationException(); // TODO REFACTOR!!!!
    }



    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private void outputCounts(HashMap<TermId,Double> hm, Ontology ontology) {
        Map mp2 = sortByValue(hm);
        for (Object t: mp2.keySet()) {
            TermId tid = (TermId) t;
            double count = (double)mp2.get(t);
            String name =  ontology.getTermMap().get(tid).getName();
            System.out.println(name + " [" +tid.getIdWithPrefix() + "]: " + count);
        }
    }


    public Set<TermId> getDescendents(Ontology ontology, TermId parent) {
        Set<TermId> descset = new HashSet<>();
        Stack<TermId> stack = new Stack<>();
        stack.push(parent);
        while (! stack.empty() ) {
            TermId tid = stack.pop();
            descset.add(tid);
            Set<TermId> directChildrenSet = getChildTerms(ontology,tid,false);
            directChildrenSet.forEach(stack::push);
        }
        return descset;
    }


    @Override public String getName() { return "hpo2hpo"; }
}
