package org.monarchinitiative.hpoworkbench.cmd;


import org.apache.log4j.Logger;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.*;
import picocli.CommandLine;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getChildTerms;

/**
 * This class drives the HPO term "cross-correlation" analysis.
 */

@CommandLine.Command(name = "hpo2hpo",
        mixinStandardHelpOptions = true,
        description = "term cross-correlation analysis.")
public class Hpo2HpoCommand extends HPOCommand implements Callable<Integer>  {
    private static Logger LOGGER = Logger.getLogger(Hpo2HpoCommand.class.getName());

    /** All of the ancestor terms of {@link #hpoTermId}. */
    private Set<TermId> descendents=null;
    @CommandLine.Option(names={"-t","--term"},required = true,description = "TermId of interest")
    private String hpoTermId;

    public Hpo2HpoCommand() {
    }



        /**
         * Function for the execution of the command.
         */
    @Override
    public Integer call() {
        inputHpoData();
        return 0;
    }

    /**
     * input the hp.obo and the annotations.
     */
    private void inputHpoData() {
        Ontology ontology = OntologyLoader.loadOntology(new File(this.hpopath));
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
        Map<TermId,Double> mp2 = sortByValue(hm);
        for (TermId t: mp2.keySet()) {
            double count = mp2.get(t);
            String name =  ontology.getTermMap().get(t).getName();
            System.out.println(name + " [" +t.getValue() + "]: " + count);
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
}
