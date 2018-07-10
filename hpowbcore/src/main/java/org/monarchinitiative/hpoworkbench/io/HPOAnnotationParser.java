package org.monarchinitiative.hpoworkbench.io;


import org.apache.log4j.Logger;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.monarchinitiative.phenol.ontology.data.TermId;


import java.util.Map;

/**
 * Use phenol to parse the HPO Annotation file.
 * called {@code phenotype.hpoa}.
 */
public class HPOAnnotationParser {
    private static final Logger LOGGER = Logger.getLogger(HPOAnnotationParser.class.getName());
    private Map<TermId,HpoDisease> diseaseMap =null;
    private final  HpoOntology ontology;
    private final String annotPath;

    public HPOAnnotationParser(String absolutePathToHpoAnnotationFile, HpoOntology ontology) throws PhenolException{
        LOGGER.trace(String.format("Initializing HPO annotation parser for %s",absolutePathToHpoAnnotationFile));
        this.annotPath=absolutePathToHpoAnnotationFile;
        this.ontology=ontology;
        parse();
    }

    public Map<TermId, HpoDisease> getDiseaseMap() {
        return diseaseMap;
    }

    private void parse() throws PhenolException{
        HpoDiseaseAnnotationParser parser = new HpoDiseaseAnnotationParser(annotPath,ontology);
        diseaseMap =parser.parse();
    }
}
