package org.monarchinitiative.hpoworkbench.analysis;

import org.monarchinitiative.hpoworkbench.exception.HPOException;
import org.monarchinitiative.phenol.formats.generic.GenericRelationship;
import org.monarchinitiative.phenol.formats.generic.GenericTerm;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.Map;

public class MondoStats {

    private final Ontology<GenericTerm,GenericRelationship> mondo;

    private int n_termCount;
    private int n_obsoleteTermCount;
    private int n_nonObsoleteTermCount;
    private int n_relations;
    private int n_definition;
    private int n_synonyms;
    private Map<String,String> metaInfo;

    public MondoStats(Ontology<GenericTerm,GenericRelationship> ont) {
        mondo=ont;
        calculateCounts();
    }


    public int getN_termCount() {
        return n_termCount;
    }

    public int getN_obsoleteTermCount() {
        return n_obsoleteTermCount;
    }

    public int getN_nonObsoleteTermCount() {
        return n_nonObsoleteTermCount;
    }

    public int getN_relations() {
        return n_relations;
    }

    public Map<String, String> getMetaInfo() {
        return metaInfo;
    }

    public int getN_definition() {
        return n_definition;
    }

    public int getN_synonyms() {
        return n_synonyms;
    }

    private void calculateCounts() {
        n_termCount= mondo.countAllTerms();
        n_nonObsoleteTermCount=mondo.countNonObsoleteTerms();
        n_obsoleteTermCount=mondo.countObsoleteTerms();
        n_relations=mondo.getRelationMap().size();
        metaInfo=mondo.getMetaInfo();
        n_definition=0;
        n_synonyms=0;
        for (TermId tid : mondo.getTermMap().keySet()) {
            GenericTerm term = mondo.getTermMap().get(tid);
            if (term.getDefinition()!=null && term.getDefinition().length()>0) n_definition++;
            if (term.getSynonyms()!=null)
                n_synonyms += term.getSynonyms().size();

        }
    }
}
