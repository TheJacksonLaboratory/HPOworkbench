package org.monarchinitiative.hpoworkbench.controller;

import com.github.phenomics.ontolib.formats.hpo.HpoTerm;

/** This class will be used to display {@link HpoTerm} objects in a tree view. The TreeView uses to
 * toString method of the objects to create the label, and the HpoTerm's toString object unfortunately
 * provides a very long string, whereas we would like  to show just the label.
 */
public class HpoTermWrapper {

    public final HpoTerm term;


    public HpoTermWrapper(HpoTerm trm) {
        this.term=trm;
    }
    @Override
    public String toString() {
        return this.term.getName();
    }

}
