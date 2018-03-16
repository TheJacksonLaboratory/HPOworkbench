package org.monarchinitiative.hpoworkbench.io;


import org.apache.log4j.Logger;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;


import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 * Use <a href="https://github.com/Phenomics/ontolib">ontolib</a> to parse the HPO Annotation file.
 * called {@code phenotype_annotation.tab}.
 */
public class HPOAnnotationParser {
    private static Logger LOGGER = Logger.getLogger(HPOAnnotationParser.class.getName());
    private Map<String,HpoDisease> disdeasenMap =null;
    private final  HpoOntology ontology;
    private final String annotPath;

    public HPOAnnotationParser(String absolutePathToHpoAnnotationFile, HpoOntology ontology) throws PhenolException{
        LOGGER.trace(String.format("Initializing HPO annotation parser for %s",absolutePathToHpoAnnotationFile));
        this.annotPath=absolutePathToHpoAnnotationFile;
        this.ontology=ontology;
        parse();
    }

    public Map<String, HpoDisease> getDisdeaseMap() {
        return disdeasenMap;
    }

    private void parse() throws PhenolException{
        File inputFile = new File(annotPath);
        HpoDiseaseAnnotationParser parser = new HpoDiseaseAnnotationParser(annotPath,ontology);
        disdeasenMap =parser.parse();
    }
}
