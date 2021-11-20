package org.monarchinitiative.hpoworkbench.resources;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.monarchinitiative.phenol.ontology.data.Ontology;

public class OptionalHpoResource implements OptionalOntologyResource {

    private final BooleanBinding hpoResourceIsMissing;

    private final ObjectProperty<Ontology> hpoOntology = new SimpleObjectProperty<>(this, "hpoOntology", null);

    public final static String HP_JSON_PATH_PROPERTY = "hp.json.path";

    public OptionalHpoResource() {
        hpoResourceIsMissing = Bindings.createBooleanBinding(() -> ontologyProperty().get()==null);
    }


    @Override
    public BooleanBinding ontologyResourceMissing() {
        return null;
    }

    @Override
    public ObjectProperty<Ontology> ontologyProperty() {
        return hpoOntology;
    }

    @Override
    public Ontology getOntology() {
        return hpoOntology.get();
    }

    @Override
    public void setOntology(Ontology ontology) {
        hpoOntology.set(ontology);
    }


}
