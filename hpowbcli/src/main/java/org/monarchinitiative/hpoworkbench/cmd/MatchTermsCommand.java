package org.monarchinitiative.hpoworkbench.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermSynonym;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Parameters(commandDescription = "match. Match HPO terms to a list of candidates.")
public class MatchTermsCommand extends  HPOCommand {
    private static final Logger logger = LogManager.getLogger();

    @Parameter(names = {"-f", "--file"},required = true, description = "TSV file with terms")
    private String path;

    private String hpopath=null;

    private Ontology hpo;

    private Map<String,TermId> labelToTermIdMap;




    public void run() {
        logger.trace("Processing input file {}", path);
        initHPOontology();
        logger.trace("Got HPO with {} terms", hpo.countAllTerms());
        try {
            String line;
            BufferedReader br = new BufferedReader(new FileReader(path));
            BufferedWriter writer = new BufferedWriter(new FileWriter("mapping.tsv"));
            while ((line=br.readLine())!= null) {
                System.err.println(line);
                String[] fields = line.split("\t");
                String label = fields[0];
                String syn = fields[1];
                System.err.println(fields.length);
                TermId tid = findBestTerm(label,syn);
                if (tid != null) {
                    String hpolabel = hpo.getTermMap().get(tid).getName();
                    String newline=String.join("\t",line,tid.getValue(),hpolabel);
                    System.err.println(newline);
                    writer.write(newline +"\n");
                } else {
                    String newline=String.join("\t",line,"n/a","n/a");
                    writer.write(newline +"\n");
                }
            }
            br.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private TermId findBestTerm(String label, String synonym) {
        TermId candidate = null;
        int  max=0;
        for (String s : labelToTermIdMap.keySet()) {
            int f = FuzzySearch.partialRatio(label,s);
            if (f>max) {
                max=f;
                candidate=labelToTermIdMap.get(s);
            }
             f = FuzzySearch.partialRatio(synonym,s);
            if (f>max) {
                max=f;
                candidate=labelToTermIdMap.get(s);
            }
        }
        if (max>80) return candidate;
        return null;
    }




    private void initHPOontology() {
        if (hpopath==null) {
            hpopath = this.downloadDirectory + File.separator + "hp.obo";
        }
        this.hpo = OntologyLoader.loadOntology(new File(this.hpopath));
        labelToTermIdMap = new HashMap<>();
        for (Term term : hpo.getTermMap().values()){
            TermId tid = term.getId();
            String label = term.getName();
            labelToTermIdMap.put(label,tid);
            for (TermSynonym tsyn :term.getSynonyms() ) {
                String syn = tsyn.getValue();
                labelToTermIdMap.put(syn,tid);
            }
        }
    }










    public String getName() {
        return "match terms";
    }



}
