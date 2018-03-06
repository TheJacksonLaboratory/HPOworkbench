package org.monarchinitiative.hpoworkbench.io;


import org.apache.log4j.Logger;
import org.monarchinitiative.phenol.formats.hpo.HpoDiseaseAnnotation;
import org.monarchinitiative.phenol.formats.hpo.HpoDiseaseWithMetadata;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.io.base.TermAnnotationParserException;
import org.monarchinitiative.phenol.io.obo.hpo.HpoAnnotation2DiseaseParser;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Use <a href="https://github.com/Phenomics/ontolib">ontolib</a> to parse the HPO Annotation file.
 * called {@code phenotype_annotation.tab}.
 */
public class HPOAnnotationParser {
    private static Logger LOGGER = Logger.getLogger(HPOAnnotationParser.class.getName());
    private List<HpoDiseaseAnnotation> annotations=null;
    Map<String,HpoDiseaseWithMetadata> annotationMap=null;
    private final  HpoOntology ontology;
    private final String annotPath;

    public HPOAnnotationParser(String absolutePathToHpoAnnotationFile, HpoOntology ontology) {
        LOGGER.trace(String.format("Initializing HPO annotation parser for %s",absolutePathToHpoAnnotationFile));
        this.annotPath=absolutePathToHpoAnnotationFile;
        this.ontology=ontology;
        parse();
    }

    public Map<String, HpoDiseaseWithMetadata> getAnnotationMap() {
        return annotationMap;
    }

    private void parse() {
        File inputFile = new File(annotPath);
        annotations=new ArrayList<>();
        HpoAnnotation2DiseaseParser parser = new HpoAnnotation2DiseaseParser(annotPath,ontology);
        annotationMap =parser.getDiseaseMap();
    }
}
