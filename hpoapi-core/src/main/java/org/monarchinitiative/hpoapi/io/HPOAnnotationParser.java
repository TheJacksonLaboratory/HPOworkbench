package org.monarchinitiative.hpoapi.io;

import com.github.phenomics.ontolib.formats.hpo.HpoDiseaseAnnotation;
import com.github.phenomics.ontolib.io.base.TermAnnotationParserException;
import com.github.phenomics.ontolib.io.obo.hpo.HpoDiseaseAnnotationParser;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Use <a href="https://github.com/Phenomics/ontolib">ontolib</a> to parse the HPO Annotation file.
 * called {@code phenotype_annotation.tab}.
 */
public class HPOAnnotationParser {
    private static Logger LOGGER = Logger.getLogger(HPOAnnotationParser.class.getName());
    List<HpoDiseaseAnnotation> annotations=null;

    public HPOAnnotationParser(String absolutePathToHpoAnnotationFile) {
        LOGGER.trace(String.format("Initializing HPO annotation parser for %s",absolutePathToHpoAnnotationFile));
        parse(absolutePathToHpoAnnotationFile);
    }

    private void parse(String path) {
        File inputFile = new File("phenotype_annotation.tab");
        try {
            HpoDiseaseAnnotationParser parser = new HpoDiseaseAnnotationParser(inputFile);
            while (parser.hasNext()) {
                HpoDiseaseAnnotation anno = parser.next();
               annotations.add(anno);
            }
        } catch (IOException e) {
            System.err.println("Problem reading from file.");
        } catch (TermAnnotationParserException e) {
            System.err.println("Problem parsing file.");
        }
    }


    public List<HpoDiseaseAnnotation> getAnnotations() {
        return annotations;
    }
}
