package org.monarchinitiative.hpoworkbench.cmd;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.log4j.Logger;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getChildTerms;

/**
 * This class drives the HPO term "cross-correlation" analysis.
 */
@Parameters(commandDescription = "hpo2hpo. term \"cross-correlation\" analysis.")
public class Hpo2HpoCommand extends HPOCommand {
    private static Logger LOGGER = Logger.getLogger(Hpo2HpoCommand.class.getName());


    private TermId termId;
    /** All of the ancestor terms of {@link #termId}. */
    private Set<TermId> descendents=null;
    @Parameter(names={"-t","--term"},required = true,description = "TermId of interest")
    private String hpoTermId;

    public Hpo2HpoCommand() {

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
        Ontology ontology = OntologyLoader.loadOntology(new File(this.hpopath));
        //diseaseMap = HpoDiseaseAnnotationParser.loadDiseaseMap(annotPath,ontology);
           // HPOAnnotationParser aparser = new HPOAnnotationParser(annotpath, ontology);

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
        Map<TermId,Double> mp2 = sortByValue(hm);
        for (Object t: mp2.keySet()) {
            TermId tid = (TermId) t;
            double count = mp2.get(t);
            String name =  ontology.getTermMap().get(tid).getName();
            System.out.println(name + " [" +tid.getValue() + "]: " + count);
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
