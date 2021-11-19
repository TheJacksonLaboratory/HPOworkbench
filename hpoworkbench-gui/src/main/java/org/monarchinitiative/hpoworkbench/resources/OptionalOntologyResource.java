package org.monarchinitiative.hpoworkbench.resources;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import org.monarchinitiative.phenol.ontology.data.Ontology;

public interface OptionalOntologyResource {

    BooleanBinding ontologyResourceMissing();

    ObjectProperty<Ontology> ontologyProperty();

    Ontology getOntology();

    void setOntology(Ontology ontology);
}
