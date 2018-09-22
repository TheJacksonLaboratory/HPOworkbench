package org.monarchinitiative.hpoworkbench.cmd;



import org.apache.log4j.Logger;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.*;
import org.monarchinitiative.phenol.io.obo.hpo.HpOboParser;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getDescendents;

/**
 * The situation is that we have a list of disease annotations (which could be {@code phenotype_annotation.tab} or
 * a smaller selection of annotations) and an HPO term. We would like to find out the total number of annotations
 * to the term or any of its ancestors. This command will outpout a list of these counts to the shell.
 * @author <a href="mailto:peter.robinson">Peter Robinson</a>
 */
public class CountFrequencyCommand extends HPOCommand {
    private static final Logger LOGGER = Logger.getLogger(DownloadCommand.class.getName());

    private final String hpOboPath;

    private final String annotationPath;

    private final TermId termId;

    private int descendentTermCount;
    /** County of annotations to any descendent of {@link #termId}. */
    private int totalAnnotationCount=0;

    private int TERMS_TO_SHOW=10;

    public CountFrequencyCommand(String hpoPath, String annotPath, String hpoTermId) {
        this.hpOboPath=hpoPath;
        this.annotationPath=annotPath;

        if (hpoTermId.startsWith("HP:")) {
            hpoTermId=hpoTermId.substring(3);
        }
        TermPrefix HP_PREFIX = new TermPrefix("HP");
        termId = new TermId(HP_PREFIX,hpoTermId);
    }

    public void run() {
        try {
            HpOboParser oparser = new HpOboParser(new File(hpOboPath));
            HpoOntology ontology = oparser.parse();


            Map<TermId, HpoDisease> annotationMap;
            try {
                HpoDiseaseAnnotationParser parser = new HpoDiseaseAnnotationParser(annotationPath, ontology);
                annotationMap = parser.parse();
                LOGGER.trace("Annotation count total " + annotationMap.size());
            } catch (PhenolException pe) {
                pe.printStackTrace();
                return;
            }
            Set<TermId> descendents = getDescendents(ontology, termId);
            descendentTermCount = descendents.size();
            LOGGER.error("Descendent Term Count size " + descendentTermCount);
            HashMap<TermId, Integer> annotationCounts = new HashMap<>();
            HashMap<TermId, Double> weightedAnnotationCounts = new HashMap<>();
            for (TermId t : descendents) {
                annotationCounts.put(t, 0);
                weightedAnnotationCounts.put(t, 0D);
            }
            for (HpoDisease d : annotationMap.values()) {
                List<HpoAnnotation> ids = d.getPhenotypicAbnormalities();
                for (HpoAnnotation tiwm : ids) {
                    TermId hpoid = tiwm.getTermId();
                    double freq = tiwm.getFrequency();
                    if (descendents.contains(hpoid)) {
                        annotationCounts.put(hpoid, 1 + annotationCounts.get(hpoid));
                        weightedAnnotationCounts.put(hpoid, freq + weightedAnnotationCounts.get(hpoid));
                        totalAnnotationCount++;
                    }
                }
            }
            outputCounts(annotationCounts, weightedAnnotationCounts, ontology);
        } catch (PhenolException | FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Could not get ontology TODO");
            LOGGER.error("Could not input ontology");
            System.exit(1);
        }
    }

    /**
     * Sort a map by values and return a sorted map with the top {@link #TERMS_TO_SHOW} items.
     * @param map Here, keys are terms and values are disease annotations
     * @param <K> key
     * @param <V> value
     * @return sorted map with top TERMS_TO_SHOW entries
     */
    private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .limit(TERMS_TO_SHOW)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private void outputCounts(HashMap<TermId,Integer> hm, Map<TermId,Double> weightedmap,Ontology ontology) {
        Map<TermId,Integer> mp2 = sortByValue(hm);
        String termS=String.format("%s [%s]",ontology.getTermMap().get(termId).getName(),
                termId.getIdWithPrefix());
        System.out.println();
        System.out.println("Annotation counts for " + termS);
        System.out.println("\tNumber of descendent terms: " + descendentTermCount);
        System.out.print(String.format("\tTotal annotations to any descendent of %s: %d ",termS, totalAnnotationCount));
        System.out.println();

        for (Object t: mp2.keySet()) {
            TermId tid = (TermId) t;
            int count = mp2.get(t);
            String name =  ontology.getTermMap().get(tid).getName();
            System.out.println(name + " [" +tid.getIdWithPrefix() + "]: " + count + " (" + weightedmap.get(tid)+")");
        }
    }


    public String getName() {
        return "count-frequency";
    }

}
