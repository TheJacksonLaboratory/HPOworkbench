package org.monarchinitiative.hpoworkbench.annotation;

import org.monarchinitiative.phenol.ontology.data.TermId;

/**
 * This class models of pair of terms from two different diseases where one term is a strict subterm of the other one.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class SubClassTermPair {

    private final TermId subTid;
    private final TermId superTid;

    public TermId getSubTid() {
        return subTid;
    }

    public TermId getSuperTid() {
        return superTid;
    }

    public SubClassTermPair(TermId tidSub, TermId tidSuper) {
        subTid=tidSub;
        superTid=tidSuper;
    }
}
