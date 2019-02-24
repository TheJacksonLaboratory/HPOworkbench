package org.monarchinitiative.hpoworkbench.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
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
@Parameters(commandDescription = "descendent. Generates a list of all terms that are descendents of a given term.")
public class HpoListDescendentsCommand extends HPOCommand  {
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

    private Ontology hpoOntology=null;
    /** Set of all HPO terms that are descendents of {@link #termOfInterest}. */
    private Set<TermId> descendentsOfTheTermOfInterest =null;
    @Parameter(names={"-t","--term"},required = true,description = "TermId of interest")
    private String hpoTermId;


    public HpoListDescendentsCommand() {
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
        if (! hpoTermId.startsWith("HP:") || hpoTermId.length()!=10) {
            LOGGER.error(String.format("Malformed HPO id: \"%s\". Terminating program...",hpoTermId ));
            System.exit(1);
        }

        // the root of the subhierarchy for which we are calculating the descriptive statistics.
        TermId termOfInterest=TermId.of(hpoTermId);
        LOGGER.trace("Term of interest: "+termOfInterest.getValue());

        inputHPOdata();
        getDescendentsOfTermOfInterest(termOfInterest);
        String desclist=descendentsOfTheTermOfInterest.stream().
                map(TermId::getValue).
                collect(Collectors.joining("\"), TermId.of(\""));
        System.out.println(desclist);

    }

    private void getDescendentsOfTermOfInterest(TermId termOfInterest) {
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
