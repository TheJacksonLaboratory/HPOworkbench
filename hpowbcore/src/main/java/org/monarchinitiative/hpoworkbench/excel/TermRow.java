package org.monarchinitiative.hpoworkbench.excel;

import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.ontology.data.TermSynonym;

import java.util.List;
import java.util.stream.Collectors;

public class TermRow {

    /** The level with respect to the initial term in the subhierarchy we are showing (which is defined as 1) */
    private final int level;
    /** HPO Term id as a String */
    private final String id;
    /** HPO Term label (name). */
    private final String label;
    /** Definition of the HPO term. */
    private  String definition;
    /** Comment, if any, associated with this HPO term. */
    private  String comment;
    /** List of synonyms (if any) of this HPO term. */
    private  String synonyms;

    public TermRow(int lev, HpoTerm term) {
        level=lev;
        id=term.getId().getIdWithPrefix();
        label=term.getName();
        definition=term.getDefinition()!=null?term.getDefinition():"currently no definition!";
        comment=term.getComment()!=null?term.getComment():"-";
        List<TermSynonym> synlist = term.getSynonyms();
        synonyms = synlist.stream().map(s -> s.getValue()).collect(Collectors.joining("; "));
        if (synonyms.isEmpty() || synonyms.length()==0) synonyms=" ";
    }



    public TermRow(int lev, HpoTerm term, String explanation) {
        this(lev,term);
        definition=explanation;
        comment="not showing descendants here";
        synonyms=" ";
    }


    public String[] getItems(int maxlevel) {
        int n=maxlevel-1+5; // number of columns needed
        String[] fields=new String[n];

        fields[level-1]=label;
        int index=maxlevel;
        fields[index]=id;

        fields[index+1]=definition;
        fields[index+2]=comment;
        fields[index+3]=synonyms;

        return fields;
    }

    public static String[] getHeader(int maxlevel) {
        int n=maxlevel-1+5; // number of columns needed
        String[] fields=new String[n];
        for (int i=0;i<maxlevel;i++) {
            fields[i]=String.format("Level %d",(i+1));
        }
        int index=maxlevel;
        fields[index]="ID";

        fields[index+1]="definition";
        fields[index+2]="comment";
        fields[index+3]="synonyms";

        return fields;
    }


}
