package org.monarchinitiative.hpoworkbench.cmd;

import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getParentTerms;


@CommandLine.Command(name = "best",
        mixinStandardHelpOptions = true,
        description = "hpo best match.")
public class HpoBestMatchCommand  extends HPOCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Hpo2HpoCommand.class);
    private Ontology hpoOntology=null;
    /** All disease annotations for the entire ontology. */
    private Map<TermId, HpoDisease> diseaseMap =null;
    @CommandLine.Option(names={"--target"},required = true,description = "file with target disease IDs")
    private String targetFile;
    @CommandLine.Option(names={"--source"},required = true,description = "file with source disease IDs")
    private String sourceFile;
    @CommandLine.Option(names={"--minhits"}, description = "minimum number of diseases in source with term")
    private int minhits = 2;

    private Set<TermId> targets;
    private Set<TermId> sources;
    /** HPO Terms present in at least {@link #minhits} source diseases. */
    private Set<TermId> sourcePhenos;

    private Set<TermId> sourcesPhenosParents;
    private Set<TermId> sourcePhenoGrandParents;


    /**
     * Function for the execution of the command.
     */
    @Override
    public Integer call() {
        inputHPOdata();
        inputTargets();
        inputSources();
        countSourcePhenos();
        getBestMatches();
        return 0;
    }


    /**
     * Get all of the matches for t that are within one hop in either direction
     * @param t TermId of an HPO term
     * @return list of matching labels
     */
    private List<String> getTwoHopMatches(TermId t) {
        List<String> hits = new ArrayList<>();
        if (sourcePhenos.contains(t)) {
            String hpolabel = hpoOntology.getTermMap().get(t).getName();
            hits.add(hpolabel);
        }
        else if (sourcesPhenosParents.contains(t)) {
            String hpolabel = hpoOntology.getTermMap().get(t).getName();
            hits.add(hpolabel + " (parent of source term)");
        } else if (sourcePhenoGrandParents.contains(t)) {
            String hpolabel = hpoOntology.getTermMap().get(t).getName();
            hits.add(hpolabel + " (grandparent of source term)");
        }
        return hits;
    }


    private List<String> getZeroHopMatches(TermId t) {
        List<String> hits = new ArrayList<>();
        if (sourcePhenos.contains(t)) {
            String hpolabel = hpoOntology.getTermMap().get(t).getName();
            hits.add(hpolabel);
        }
        return hits;
    }


    private void getBestMatches() {
        for (TermId diseaseId : targets) {
            if (diseaseMap.containsKey(diseaseId)) {
                HpoDisease disease = diseaseMap.get(diseaseId);
                List<TermId> hpos = disease.getPhenotypicAbnormalityTermIdList();
                for (TermId hpo : hpos) {
                   List<String> hits = getTwoHopMatches(hpo);
                   if (hits.isEmpty()) {
                       Set<TermId> pars = getParentTerms(hpoOntology,hpo,false);
                       for (TermId par : pars) {
                           List<String> newHits1 = getZeroHopMatches(par);
                           hits.addAll(newHits1);
                       }
                       if (hits.isEmpty()) {
                           Set<TermId> gpars = getParentTerms(hpoOntology,pars,false);
                           for (TermId par : gpars) {
                               List<String> newHits1 = getZeroHopMatches(par);
                               hits.addAll(newHits1);
                           }
                       }
                   }
                   for (String h : hits) {
                       System.out.println(disease.getName() +": " + h);
                   }
                }
            } else {
                System.err.println("[ERROR] could not find disease for target " + diseaseId.getValue());
            }
        }
    }



    private void countSourcePhenos() {
        Map<TermId,Integer> counts = new HashMap<>();
        for (TermId tid : sources) {
            if (diseaseMap.containsKey(tid)) {
                HpoDisease disease = diseaseMap.get(tid);
                List<TermId> hpos = disease.getPhenotypicAbnormalityTermIdList();
                for (TermId hpo : hpos) {
                    counts.putIfAbsent(hpo,0);
                    int c = counts.get(hpo);
                    counts.put(hpo,c+1);
                }
            } else {
                System.err.println("[ERROR] could not find disease for " + tid.getValue());
            }
        }
        System.out.println("[INFO] Got counts for " + counts.size() + " HPO terms");
        sourcePhenos = new HashSet<>();
        for (TermId hpo : counts.keySet()) {
            int c = counts.get(hpo);
            if (c>=minhits) {
                sourcePhenos.add(hpo);
            }
        }
        for (TermId t : sourcePhenos) {
            String label = hpoOntology.getTermMap().get(t).getName();
            System.out.println("\t"+label);
        }
        sourcesPhenosParents = getParentTerms(hpoOntology,sourcePhenos,false);
        sourcePhenoGrandParents = getParentTerms(hpoOntology,sourcesPhenosParents,false);
    }


    private void inputTargets() {
        targets = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.targetFile));
            String line;
            while ((line=br.readLine())!=null) {
                if (line.startsWith("OMIM")) {
                    TermId diseaseId=TermId.of(line.trim());
                    targets.add(diseaseId);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("[INFO] target diseases: " + targets.size());
    }

    private void inputSources() {
        sources = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.sourceFile));
            String line;
            while ((line=br.readLine())!=null) {
                if (line.startsWith("OMIM")) {
                    TermId diseaseId=TermId.of(line.trim());
                    sources.add(diseaseId);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("[INFO] sources diseases: " + sources.size());
    }





    private void inputHPOdata() {
        File f = new File(hpopath);
        if (! f.exists()) {
            LOGGER.error(String.format("Could not find hpo ontology file at\"%s\". Terminating program...", hpopath ));
            System.exit(1);
        }
        f=new File(annotpath);
        if (! f.exists()) {
            LOGGER.error(String.format("Could not find phenotype annotation file at\"%s\". Terminating program...", annotpath ));
            System.exit(1);
        }
        LOGGER.trace(String.format("inputting data with files %s and %s",hpopath,annotpath));
        HPOParser parser = new HPOParser(hpopath);
        hpoOntology=parser.getHPO();
        diseaseMap = HpoDiseaseAnnotationParser.loadDiseaseMap(annotpath,hpoOntology);
        LOGGER.trace("Diseases imported: " + diseaseMap.size());
    }


}
