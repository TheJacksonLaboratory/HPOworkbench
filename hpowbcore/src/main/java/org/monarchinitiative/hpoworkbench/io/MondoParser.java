package org.monarchinitiative.hpoworkbench.io;

import org.apache.log4j.Logger;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.generic.GenericRelationship;
import org.monarchinitiative.phenol.formats.generic.GenericTerm;
import org.monarchinitiative.phenol.io.obo.hpo.HpoOboParser;
import org.monarchinitiative.phenol.io.owl.OwlImmutableOntologyLoader;
import org.monarchinitiative.phenol.io.owl.generic.GenericOwlFactory;
import org.monarchinitiative.phenol.ontology.data.ImmutableOntology;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.io.IOException;

public class MondoParser {
    private static Logger LOGGER = Logger.getLogger(MondoParser.class.getName());

    private final String mondoOboPath;

    private Ontology<GenericTerm, GenericRelationship> mondo=null;


    public MondoParser(String pathToMondoObo) throws PhenolException {
        mondoOboPath=pathToMondoObo;
        parse();
    }



    public Ontology<GenericTerm, GenericRelationship> getMondo() {
        return mondo;
    }

    public void parse() throws PhenolException {
        LOGGER.trace("Parsing mondo obo file at " + mondoOboPath);
        final OwlImmutableOntologyLoader<GenericTerm, GenericRelationship> loader =
                new OwlImmutableOntologyLoader<>(
                        new File(mondoOboPath));

        final GenericOwlFactory cof = new GenericOwlFactory();
        try {
            mondo= loader.load(cof);
        } catch (OWLOntologyCreationException | IOException e) {
            throw new PhenolException(String.format("Could not create Mondo ontology: %s",e.getMessage() ));
        }
    }



}
