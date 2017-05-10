package org.monarch.hpoapi.data;

import org.monarch.hpoapi.association.Association;
import org.monarch.hpoapi.association.AssociationContainer;
import org.monarch.hpoapi.ontology.Ontology;

/**
 * This class intends to encapsulate the {@link org.monarch.hpoapi.ontology.Ontology} and the
 * {@link org.monarch.hpoapi.association.AssociationContainer} objects for the Ontology that has
 * been parsed in. Currently, we only support Human Phenotype Ontology, but this interface is
 * intended to be flexible enough to allow parsing of the equivalent MPO io and perhaps of other
 * phenotype ontologies.
 * Created by peter on 09.05.17.
 */
public class PhenotypeData {

    private Ontology ontology;

    private AssociationContainer container;

    public PhenotypeData(Ontology o, AssociationContainer ac) {
        this.ontology=o;
        this.container = ac;
    }

    public Ontology getOntology() { return ontology;}

    public AssociationContainer getAssociationContainer() { return container; }



}
