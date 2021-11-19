package org.monarchinitiative.hpoworkbench.controller;


import org.monarchinitiative.phenol.ontology.data.Term;

/** This class will be used to display {@link Term} objects in a tree view. The TreeView uses to
 * toString method of the objects to create the label, and the HpoTerm's toString object unfortunately
 * provides a very long string, whereas we would like  to show just the label.
 */
class OntologyTermWrapper {

    final Term term;


    OntologyTermWrapper(Term trm) {
        this.term=trm;
    }
    @Override
    public String toString() {
        return this.term.getName();
    }

}
