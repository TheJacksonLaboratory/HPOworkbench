package org.monarchinitiative.hpoworkbench.cmd;

import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.LogManager;
import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.phenol.graph.IdLabeledEdge;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Generates a list of all terms that are descendents of a given term.
 */
public class HpoListDescendentsCommand extends HPOCommand  {
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    private final String hpopath;
    /** the root of the subhierarchy for which we are calculating the descriptive statistics. */
    private final TermId termOfInterest;
    private Ontology hpoOntology=null;
    /** Set of all HPO terms that are descendents of {@link #termOfInterest}. */
    private Set<TermId> descendentsOfTheTermOfInterest =null;

    public HpoListDescendentsCommand(String hpo,String term) {
        this.hpopath=hpo;
        if (! term.startsWith("HP:") || term.length()!=10) {
            LOGGER.error(String.format("Malformed HPO id: \"%s\". Terminating program...",term ));
            System.exit(1);
        }
        this.termOfInterest=TermId.of(term);
        LOGGER.trace("Term of interest: "+termOfInterest.getValue());

        inputHPOdata();
    }

    private void inputHPOdata() {
        File f = new File(hpopath);
        if (! f.exists()) {
            LOGGER.error(String.format("Could not find hpo ontology file at\"%s\". Terminating program...", hpopath ));
            System.exit(1);
        }
        HPOParser parser = new HPOParser(hpopath);
        hpoOntology=parser.getHPO();
    }

    @Override
    public  void run() {
        getDescendentsOfTermOfInterest();
        String desclist=descendentsOfTheTermOfInterest.stream().
                map(TermId::getValue).
                collect(Collectors.joining("\"), TermId.of(\""));
        System.out.println(desclist);

    }

    private void getDescendentsOfTermOfInterest() {
        String name = String.format("%s [%s]",hpoOntology.getTermMap().get(termOfInterest).getName(),termOfInterest.getValue() );
        descendentsOfTheTermOfInterest = countDescendentsAndSubclassRelations(hpoOntology,termOfInterest);


    }

    /**
     * Find all of the descendents of parentTermId (including direct children and more distant
     * descendents)
     *
     * @param ontology The ontology to which parentTermId belongs
     * @param parentTermId The term whose descendents were are seeking
     * @return A set of all descendents of parentTermId (including the parentTermId itself)
     */
    private Set<TermId> countDescendentsAndSubclassRelations(
            Ontology ontology, TermId parentTermId) {
        ImmutableSet.Builder<TermId> descset = new ImmutableSet.Builder<>();
        Stack<TermId> stack = new Stack<>();
        stack.push(parentTermId);
        while (!stack.empty()) {
            TermId tid = stack.pop();
            descset.add(tid);
            Set<TermId> directChildrenSet = countChildTermsAndSubclassRelations(ontology, tid);
            directChildrenSet.forEach(stack::push);
        }
        return descset.build();
    }

    /**
     * Find all of the direct children of parentTermId (do not include "grandchildren" and other
     * descendents).
     *
     * @param ontology The ontology to which parentTermId belongs
     * @param parentTermId The term whose children were are seeking
     * @return A set of all child terms of parentTermId
     */
    public Set<TermId> countChildTermsAndSubclassRelations(
            Ontology ontology,
            TermId parentTermId) {
        ImmutableSet.Builder<TermId> kids = new ImmutableSet.Builder<>();
        //if (includeOriginalTerm) kids.add(parentTermId);
        for (IdLabeledEdge edge : ontology.getGraph().incomingEdgesOf(parentTermId)) {
            TermId sourceId = (TermId) edge.getSource();
            kids.add(sourceId);
        }
        return kids.build();
    }



    @Override
    public String getName() {return "list descendents";}

}
