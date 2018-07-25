package org.monarchinitiative.hpoworkbench.io;

import org.apache.log4j.Logger;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.io.owl.OwlImmutableOntologyLoader;

import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;


public class MondoParser {
    private static final Logger LOGGER = Logger.getLogger(MondoParser.class.getName());

    private final String mondoOboPath;

    private Ontology mondo=null;


    public MondoParser(String pathToMondoObo) throws PhenolException {
        mondoOboPath=pathToMondoObo;
        parse();
    }



    public Ontology getMondo() {
        return mondo;
    }

    private void parse() {
        LOGGER.trace("Parsing mondo obo file at " + mondoOboPath);
        final OwlImmutableOntologyLoader loader =
                new OwlImmutableOntologyLoader(
                        new File(mondoOboPath));
        try {
            mondo = loader.load();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();

        }
    }



}
