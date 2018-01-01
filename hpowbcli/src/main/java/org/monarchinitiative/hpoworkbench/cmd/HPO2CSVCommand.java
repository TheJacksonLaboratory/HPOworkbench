package org.monarchinitiative.hpoworkbench.cmd;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.ontology.data.TermId;
import org.apache.log4j.Logger;



import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.monarchinitiative.hpoworkbench.argparser.ArgumentParserException;
import org.monarchinitiative.hpoworkbench.io.HPOParser;

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

    private String pathToHpObo=null;


    /**
     *
     */
    public HPO2CSVCommand()  {
        //this.options = new PhenotypeDownloadOptions();
       // this.options.setFromArgs(args);
    }

    /** This function passes the options to the command
     * and makes sure we have everything we need. It checks
     * the default values if it doesnt find the values here.
     * If it is missing something, it throws and exception.
     * @param mp
     */
    public void setOptions(Map<String,String> mp) throws ArgumentParserException {
        LOGGER.trace("setOptions CSV");
        for (String s:mp.keySet()) {
            LOGGER.trace("\t"+s+":"+mp.get(s));
        }
        if (mp.containsKey("input")) {
            pathToHpObo=mp.get("input");
        } else if (this.defaults.containsKey("input")) {
            pathToHpObo=defaults.get("input");
        } else {
            throw new ArgumentParserException("--input option must be provided to run csv");
        }
        //todo -- output file path
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
    public static final String header="#id\tname\tdef\tsynonyms\txrefs\tis_a";



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
