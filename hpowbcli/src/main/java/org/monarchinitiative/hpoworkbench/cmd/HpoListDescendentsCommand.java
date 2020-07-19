package org.monarchinitiative.hpoworkbench.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.collect.ImmutableSet;
import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.phenol.annotations.assoc.HpoAssociationParser;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.graph.IdLabeledEdge;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Generates a list of all terms that are descendents of a given term.
 */
@Parameters(commandDescription = "descendent. Generates a list of all terms that are descendents of a given term.")
public class HpoListDescendentsCommand extends HPOCommand  {
    private static final Logger LOGGER = LoggerFactory.getLogger(HpoListDescendentsCommand.class);

    private Ontology hpoOntology=null;
    /** Set of all HPO terms that are descendents of {@link #hpoTermId}. */
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

    private void parsePhenotypeHpoa() {
        String path = "data/phenotype.hpoa";
        File f = new File(path);
        if (!f.exists()) {
            throw new PhenolRuntimeException("Could not find phenotype.hpoa. Run the download command");
        }
        int n_omim = 0;
        int n_orpha = 0;
        int n_decipher = 0;
        int n_omim_total = 0;
        int n_orpha_total = 0;
        int n_decipher_total = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line=br.readLine()) != null) {
                //System.out.println(line);
                if (line.startsWith("#") ) {
                    continue; // skip comments
                }
                if (line.startsWith("OMIM")) n_omim_total++;
                if (line.startsWith("ORPHA")) n_orpha_total++;
                if (line.startsWith("DECIPHER")) n_decipher_total++;
                String [] fields = line.split("\t");
                TermId t = TermId.of(fields[3]);
                System.out.println(t.getValue());
                if (descendentsOfTheTermOfInterest.contains(t)) {
                    if (line.startsWith("OMIM")){
                        n_omim++;
                    } else if (line.startsWith("ORPHA")) {
                        n_orpha++;
                    } else if (line.startsWith("DECIPHER")) {
                        n_decipher++;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Total of %d terms descend from %s\n", descendentsOfTheTermOfInterest.size(), hpoTermId);
        System.out.printf("Annotations: OMIM: %d, ORPHA: %d, DECIPHER: %d\n", n_omim, n_orpha, n_decipher);
        System.out.printf("Percent omim %f%% (%d/%d)\n",(100.0*(double)n_omim/n_omim_total),n_omim,n_omim_total);
        System.out.printf("Percent orpha %f%% (%d/%d)\n",(100.0*(double)n_orpha/n_orpha_total),n_orpha,n_orpha_total);
        System.out.printf("Percent decipher %f%% (%d/%d)\n",(100.0*(double)n_decipher/n_decipher_total),n_decipher,n_decipher_total);
        int prenatal = n_omim + n_orpha + n_decipher;
        int total = n_omim_total + n_orpha_total + n_decipher_total;
        System.out.printf("Total: %f%% (%d/%d)\n",(100.0*(double)prenatal/total),prenatal,total);

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
        parsePhenotypeHpoa();

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
