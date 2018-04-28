package org.monarchinitiative.hpoworkbench.cmd;


import org.apache.log4j.Logger;



import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.HpoTerm;
import org.monarchinitiative.phenol.ontology.data.TermId;

/**
 * Make a CSV file representing the HPO hp.obo file
 * Created by robinp on 6/23/17.
 */
public class HPO2CSVCommand extends HPOCommand {
    private static Logger LOGGER = Logger.getLogger(HPO2CSVCommand.class.getName());
    /** name of this command */
    private final static String name = "csv";
    private Map<String,String> hpoName2IDmap=null;

    public String getName() { return name; }

    private final String pathToHpObo;


    /**
     *
     */
    public HPO2CSVCommand(String hpOboPath)  {
        this.pathToHpObo=hpOboPath;
    }




    /**
     * id: HP:3000067
     name: Abnormality of lateral crico-arytenoid
     def: "An abnormality of a lateral crico-arytenoid." [GOC:TermGenie]
     synonym: "Abnormality of lateral cricoarytenoid muscle" EXACT []
     xref: UMLS:C4073274
     is_a: HP:0000464 ! Abnormality of the neck
     is_a: HP:0003011 ! Abnormality of the musculature
     is_a: HP:0025423 ! Abnormal larynx morphology
     TODO 	def not working,
     */
    private static final String header="#id\tname\tdef\tsynonyms\txrefs\tis_a";



    /**
     * Perform the downloading.
     */
    @Override
    public void run()  {
        HpoOntology ontology=null;
        HPOParser hpoparser=new HPOParser(pathToHpObo);
        try {
            ontology = hpoparser.getHPO();
        } catch (Exception e) {
            System.err.println("[ERROR] could not partse hp.obo file.\n"+e.toString() );
            System.exit(1);
        }

        Collection<HpoTerm>  terms = ontology.getTerms();

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("hp.tsv"));
            bw.write(header+"\n");
           for (HpoTerm t :terms) {
                //System.out.println(t);
                String label = t.getName().toString();
                String id = t.getId().getIdWithPrefix();
                String def=t.getDefinition();
                String synString=t.getSynonyms().stream().map(s->s.getValue()).collect(Collectors.joining("; "));
              Set<TermId> ancestors= ontology.getAncestorTermIds(t.getId());

             /* for (TermId i:ancestors) {
                  Term = ontology.getterm(id);
              }

              t.getSubsets()

                ParentTermID [] parents = t.getParents();
                String parentsString=join(parents);
                bw.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\n",label,id,defString,synString,xrefString,parentsString));
                //System.out.println(String.format("%s\t%s\t%s\t%s\t%s\t%s\n",label,id,defString,synString,xrefString,parentsString));
                */
            }

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


}
