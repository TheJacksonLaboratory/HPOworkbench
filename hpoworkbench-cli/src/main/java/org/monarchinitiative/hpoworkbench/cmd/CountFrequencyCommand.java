package org.monarchinitiative.hpoworkbench.cmd;

import org.monarchinitiative.phenol.annotations.formats.hpo.*;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoader;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoaderOptions;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoaders;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getDescendents;

/**
 * The situation is that we have a list of disease annotations (which could be {@code phenotype_annotation.tab} or
 * a smaller selection of annotations) and an HPO term. We would like to find out the total number of annotations
 * to the term or any of its ancestors. This command will outpout a list of these counts to the shell.
 *
 * @author <a href="mailto:peter.robinson">Peter Robinson</a>
 */

@CommandLine.Command(name = "countfreq",
        mixinStandardHelpOptions = true,
        description = "Count freqeuncy of annotations.")
public class CountFrequencyCommand extends HPOCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadCommand.class.getName());

    @CommandLine.Option(names={"-t","--term"},required = true,description = "TermId of interest")
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

    public Integer call() throws IOException {
        String hpJsonPath = this.downloadDirectory + File.separator + this.hpopath;
        String annotationPath = this.downloadDirectory + File.separator + annotpath;

        Ontology ontology = OntologyLoader.loadOntology(new File(hpJsonPath));
        HpoDiseaseLoaderOptions options = HpoDiseaseLoaderOptions.defaultOptions();
        HpoDiseaseLoader loader = HpoDiseaseLoaders.defaultLoader(ontology, options);
        HpoDiseases diseases = loader.load(Path.of(annotationPath));
        termId = TermId.of(hpoTermId);

        Map<TermId, HpoDisease> annotationMap = diseases.diseaseById();
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
            for (HpoDiseaseAnnotation hpoDiseaseAnnot : d.annotations()) {
                TermId hpoid = hpoDiseaseAnnot.id();
                float freq = hpoDiseaseAnnot.frequency();
                if (descendents.contains(hpoid)) {
                    annotationCounts.put(hpoid, 1 + annotationCounts.get(hpoid));
                    weightedAnnotationCounts.put(hpoid, freq + weightedAnnotationCounts.get(hpoid));
                    totalAnnotationCount++;
                }
            }
        }
        outputCounts(annotationCounts, weightedAnnotationCounts, ontology);
        return 0;
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
        System.out.printf("\tTotal annotations to any descendent of %s: %d ", termS, totalAnnotationCount);
        System.out.println();

        for (TermId tid : mp2.keySet()) {
            int count = mp2.get(tid);
            String name = ontology.getTermMap().get(tid).getName();
            System.out.println(name + " [" + tid.getValue() + "]: " + count + " (" + weightedmap.get(tid) + ")");
        }
    }


    public String getName() {
        return "count-frequency";
    }

}
