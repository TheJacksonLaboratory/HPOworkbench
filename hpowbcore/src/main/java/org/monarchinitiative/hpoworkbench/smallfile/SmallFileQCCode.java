package org.monarchinitiative.hpoworkbench.smallfile;

public enum SmallFileQCCode {
    DID_NOT_FIND_EVIDENCE_CODE("Didnt find valid evidence code"),
    GOT_GENE_DATA("Found and discarded gene data"),
    UPDATING_ALT_ID("Updated alt_id to current primary id"),
    UPDATING_HPO_LABEL("Updated label to current label"),
    CREATED_MODIFER("Created modifier term"),
    UPDATED_DATE_FORMAT("Updated created-by date to canonical date format"),
    GOT_EQ_ITEM("Found and discarded EQ items");

    private final String name;

    SmallFileQCCode(String n) { name=n;}

    public String getName() { return name;}
}
