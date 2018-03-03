package org.monarchinitiative.hpoworkbench.io;


import org.apache.log4j.Logger;
import org.monarchinitiative.phenol.formats.hpo.HpoDiseaseAnnotation;
import org.monarchinitiative.phenol.io.base.TermAnnotationParserException;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Use <a href="https://github.com/Phenomics/ontolib">ontolib</a> to parse the HPO Annotation file.
 * called {@code phenotype_annotation.tab}.
 */
public class HPOAnnotationParser {
    private static Logger LOGGER = Logger.getLogger(HPOAnnotationParser.class.getName());
    private List<HpoDiseaseAnnotation> annotations=null;

    public HPOAnnotationParser(String absolutePathToHpoAnnotationFile) {
        LOGGER.trace(String.format("Initializing HPO annotation parser for %s",absolutePathToHpoAnnotationFile));
        parse(absolutePathToHpoAnnotationFile);
    }

    private void parse(String path) {
        File inputFile = new File(path);
        annotations=new ArrayList<>();
        try {
            HpoDiseaseAnnotationParser parser = new HpoDiseaseAnnotationParser(inputFile);
            while (parser.hasNext()) {
                HpoDiseaseAnnotation anno = parser.next();
               annotations.add(anno);
            }
        } catch (IOException e) {
            LOGGER.error("Could not read from file at \""+path+"\"");
            LOGGER.error(e,e);
        } catch (TermAnnotationParserException e) {
            LOGGER.error("Could not parse file at "+path);
            LOGGER.error(e,e);
        }
    }


    public List<HpoDiseaseAnnotation> getAnnotations() {
        return annotations;
    }
}
