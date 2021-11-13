package org.monarchinitiative.hpoworkbench.io;



import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class MondoParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MondoParser.class);

    private final String mondoOboPath;

    private Ontology mondo=null;


    public MondoParser(String pathToMondoObo) {
        mondoOboPath=pathToMondoObo;
        parse();
    }



    public Ontology getMondo() {
        return mondo;
    }

    private void parse() {
        LOGGER.trace("Parsing mondo obo file at " + mondoOboPath);
        mondo = OntologyLoader.loadOntology(new File(mondoOboPath));
    }



}
