package org.monarchinitiative.hpoworkbench.io;


import org.apache.log4j.Logger;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;


import java.util.Map;

/**
 * Use <a href="https://github.com/Phenomics/ontolib">ontolib</a> to parse the HPO Annotation file.
 * called {@code phenotype_annotation.tab}.
 */
public class HPOAnnotationParser {
    private static Logger LOGGER = Logger.getLogger(HPOAnnotationParser.class.getName());
    Map<String,HpoDisease> annotationMap=null;

    private final  HpoOntology ontology;
    private final String annotPath;

    public HPOAnnotationParser(String absolutePathToHpoAnnotationFile, HpoOntology ontology) {
        LOGGER.trace(String.format("Initializing HPO annotation parser for %s",absolutePathToHpoAnnotationFile));
        this.annotPath=absolutePathToHpoAnnotationFile;
        this.ontology=ontology;
        parse();
    }

    public Map<String, HpoDisease> getAnnotationMap() {
        return annotationMap;
    }

    private void parse() {
        HpoDiseaseAnnotationParser parser = new HpoDiseaseAnnotationParser(annotPath,ontology);
        try {
            annotationMap = parser.parse();
        } catch (PhenolException e) {
            e.printStackTrace();
        }
    }
}
