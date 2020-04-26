package org.monarchinitiative.hpoworkbench.cmd;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.log4j.Logger;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getDescendents;

/**
 * The situation is that we have a list of disease annotations (which could be {@code phenotype_annotation.tab} or
 * a smaller selection of annotations) and an HPO term. We would like to find out the total number of annotations
 * to the term or any of its ancestors. This command will outpout a list of these counts to the shell.
 *
 * @author <a href="mailto:peter.robinson">Peter Robinson</a>
 */
@Parameters(commandDescription = "countfreq. Count freqeuncy of annotations.")
public class CountFrequencyCommand extends HPOCommand {
    private static final Logger LOGGER = Logger.getLogger(DownloadCommand.class.getName());

    @Parameter(names={"-t","--term"},required = true,description = "TermId of interest")
    private String hpoTermId;

    private TermId termId;

    private int descendentTermCount;
    /**
     * County of annotations to any descendent of {@link #termId}.
     */
    private int totalAnnotationCount = 0;

    private int TERMS_TO_SHOW = 10;

    public CountFrequencyCommand() {

    }

    public void run() {

        String hpOboPath = this.downloadDirectory + File.separator + this.hpopath;
        String annotationPath = this.downloadDirectory + File.separator + annotpath;
        termId = TermId.of(hpoTermId);


        Ontology ontology = OntologyLoader.loadOntology(new File(hpOboPath));

        Map<TermId, HpoDisease> annotationMap;
        annotationMap = HpoDiseaseAnnotationParser.loadDiseaseMap(annotationPath, ontology);
        LOGGER.trace("Annotation count total " + annotationMap.size());
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

    }

    /**
     * Sort a map by values and return a sorted map with the top {@link #TERMS_TO_SHOW} items.
     *
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

    private void outputCounts(HashMap<TermId, Integer> hm, Map<TermId, Double> weightedmap, Ontology ontology) {
        Map<TermId, Integer> mp2 = sortByValue(hm);
        String termS = String.format("%s [%s]", ontology.getTermMap().get(termId).getName(),
                termId.getValue());
        System.out.println();
        System.out.println("Annotation counts for " + termS);
        System.out.println("\tNumber of descendent terms: " + descendentTermCount);
        System.out.print(String.format("\tTotal annotations to any descendent of %s: %d ", termS, totalAnnotationCount));
        System.out.println();

        for (Object t : mp2.keySet()) {
            TermId tid = (TermId) t;
            int count = mp2.get(t);
            String name = ontology.getTermMap().get(tid).getName();
            System.out.println(name + " [" + tid.getValue() + "]: " + count + " (" + weightedmap.get(tid) + ")");
        }
    }


    public String getName() {
        return "count-frequency";
    }

}
