package org.monarchinitiative.hpoworkbench.analysis;

import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HpoStats {
    private static final Logger LOGGER = LoggerFactory.getLogger(HpoStats.class);
    private final Ontology hpoOntology;
    /**
     * All disease annotations for the entire ontology.
     */
    private final Map<TermId, HpoDisease> diseaseMap;

    public HpoStats(String hpoOboPath, String annotpath) {
        File f = new File(hpoOboPath);
        if (!f.exists()) {
            LOGGER.error(String.format("Could not find hpo ontology file at\"%s\". Terminating program...", hpoOboPath));
            System.exit(1);
        }
        f = new File(annotpath);
        if (!f.exists()) {
            LOGGER.error(String.format("Could not find phenotype annotation file at\"%s\". Terminating program...", annotpath));
            System.exit(1);
        }
        LOGGER.trace(String.format("inputting data with files %s and %s", hpoOboPath, annotpath));
        HPOParser parser = new HPOParser(hpoOboPath);
        this.hpoOntology = parser.getHPO();

        this.diseaseMap = HpoDiseaseAnnotationParser.loadDiseaseMap(Path.of(annotpath), hpoOntology);
        LOGGER.trace("Diseases imported: " + diseaseMap.size());
    }

    public void outputOntologyStats(Writer writer) {
        Set<TermId> nonObsolete = this.hpoOntology.getNonObsoleteTermIds();
        int n_terms = nonObsolete.size();
        int n_synonyms = this.hpoOntology.getTermMap()
                .values()
                .stream()
                .map(Term::getSynonyms)
                .map(List::size)
                .reduce(0, Integer::sum);
        Ontology phenoAbn = this.hpoOntology.subOntology(TermId.of("HP:0000118")); // phenotypic abnormality subontoloy
        int n_phenoAbnTerms = phenoAbn.countNonObsoleteTerms();
        try {
            writer.write("##########################################\n");
            writer.write("Number of non-obsolete terms: " + n_terms + "\n");
            writer.write("Number of non-obsolete terms in Phenotypic Abnormality subontology: " + n_phenoAbnTerms + "\n");
            writer.write("Number of synonyms: " + n_synonyms + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
