package org.monarchinitiative.hpoworkbench.word;


import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermSynonym;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class HpoRtfTableRow {
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


    HpoRtfTableRow(int lev, Term term) {
        level=lev;
        id=term.id().getValue();
        label=term.getName();
        definition=term.getDefinition()!=null?term.getDefinition():"\\b \\i needs definition! \\i0 \\b0";
        comment=term.getComment()!=null?term.getComment():"-";
        List<TermSynonym> synlist = term.getSynonyms();
        synonyms = synlist.stream().map(TermSynonym::getValue).collect(Collectors.joining("; "));
        if (synonyms.isEmpty()) synonyms=" ";
    }


    HpoRtfTableRow(int lev, Term term, String explanation) {
        this(lev,term);
        definition=explanation;
        comment="not showing descendants here";
        synonyms=" ";


    }



    /**
     * @return A Table header for showing HPO terms.
     */
    static String header() {
        return """
                \\trowd
                \\cellx1000
                \\cellx3000
                \\cellx6000
                \\cellx9000
                \\b term\\b0\\intbl\\cell
                \\b definition\\b0\\intbl\\cell
                \\b comment\\b0\\intbl\\cell
                \\b synonyms\\b0 \\intbl\\cell
                \\row""";
    }

    /**
     *
     * @return One RTF table row corresponding to an HPO term.
     */
    String row() {
        String levelAndId = getLevelAndId();
        return String.format("""
                \\trowd
                \\cellx1000
                \\cellx3000
                \\cellx6000
                \\cellx9000
                %s\\intbl\\cell
                %s\\intbl\\cell
                %s\\intbl\\cell
                %s\\intbl\\cell
                \\row""", levelAndId, definition, comment,synonyms);
    }

    /**
     * Create the contents of the first cell of the table row, something like ------- label [id].
     * @return level of term in hierarchy starting from reference term.
     */
    private String getLevelAndId() {
        char[] chars = new char[3 * level];
        char c = '-';
        Arrays.fill(chars, c);
        return String.format("%s %s [%s]", String.valueOf(chars), label, id);
    }


}
