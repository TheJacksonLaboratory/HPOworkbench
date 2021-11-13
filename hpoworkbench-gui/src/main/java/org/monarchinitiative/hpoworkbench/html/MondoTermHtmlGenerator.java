package org.monarchinitiative.hpoworkbench.html;

import org.monarchinitiative.phenol.ontology.data.Dbxref;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermSynonym;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Convenience class to generate HTML code for the header with a Mondo term.
 */
public class MondoTermHtmlGenerator {
    private final static String EMPTY_STRING="";

    private final static String TEMPLATE="<h1>%s</h1>\n" +
            "            <p><b>ID:</b> %s</p>\n" +
            "            <p><b>Definition:</b> %s</p>\n" +
            "            <p><b>Dbxref:</b> %s</p>" +
            "            <p><b>Synonyms:</b> %s</p>\n"+
            "            <p><b>Comment:</b> %s</p>\n";


    public static String getHTML(Term mondoTerm) {
        String termID = mondoTerm.getId().getValue();
        String synonyms = (mondoTerm.getSynonyms() == null) ? EMPTY_STRING : mondoTerm.getSynonyms().stream().map(TermSynonym::getValue)
                .collect(Collectors.joining("; "));
        String definition = (mondoTerm.getDefinition() == null) ? EMPTY_STRING : mondoTerm.getDefinition();
        List<Dbxref> dbxlist = mondoTerm.getXrefs();

        String dbxString = (dbxlist == null || dbxlist.isEmpty()) ?
                EMPTY_STRING :
                dbxlist.stream().map(Dbxref::getName).collect(Collectors.joining("; "));
        return String.format(TEMPLATE,mondoTerm.getName(),termID,definition,dbxString,synonyms,mondoTerm.getComment());
    }
}
