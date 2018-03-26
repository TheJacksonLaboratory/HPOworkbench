package org.monarchinitiative.hpoworkbench.annotation;

import org.monarchinitiative.phenol.ontology.data.TermId;

public class TermSubClassPair {

    private final TermId subTid;
    private final TermId superTid;

    public TermId getSubTid() {
        return subTid;
    }

    public TermId getSuperTid() {
        return superTid;
    }

    public TermSubClassPair(TermId tidSub, TermId tidSuper) {
        subTid=tidSub;
        superTid=tidSuper;

    }


}
