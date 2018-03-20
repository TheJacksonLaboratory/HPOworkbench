package org.monarchinitiative.hpoworkbench.io;

import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.generic.GenericRelationship;
import org.monarchinitiative.phenol.formats.generic.GenericTerm;
import org.monarchinitiative.phenol.io.owl.OwlImmutableOntologyLoader;
import org.monarchinitiative.phenol.io.owl.generic.GenericOwlFactory;
import org.monarchinitiative.phenol.ontology.data.ImmutableOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.io.IOException;

public class MondoParser {

    private final String pathToMondoFile;

    public MondoParser(String pathToMondo) {
        pathToMondoFile =pathToMondo;
    }


    public ImmutableOntology<GenericTerm,GenericRelationship> parse() throws PhenolException{
        final OwlImmutableOntologyLoader<GenericTerm, GenericRelationship> loader =
                new OwlImmutableOntologyLoader<GenericTerm, GenericRelationship>(
                        new File(pathToMondoFile));

        final GenericOwlFactory cof = new GenericOwlFactory();
        try {
            final ImmutableOntology<GenericTerm, GenericRelationship> ontology = loader.load(cof);
            return ontology;
        } catch (OWLOntologyCreationException | IOException e) {
            throw new PhenolException(String.format("Could not create Mondo ontology: %s",e.getMessage() ));
        }
    }



}
