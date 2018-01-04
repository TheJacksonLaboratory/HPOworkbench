package org.monarchinitiative.hpoworkbench.controller;

import com.github.phenomics.ontolib.formats.hpo.HpoTerm;

import java.util.List;
import java.util.stream.Collectors;

public class HpoHtmlPageGenerator {

    public static String getHTML(HpoTerm term, List<String> annotatedDiseases) {

        String termID = term.getId().getIdWithPrefix();
        String synonyms = (term.getSynonyms() == null) ? "" : term.getSynonyms().stream().map(s -> s.getValue())
                .collect(Collectors.joining("; "));
        // Synonyms
        String definition = (term.getDefinition() == null) ? "" : term.getDefinition().toString();
        String comment = (term.getComment() == null) ? "-" : term.getComment();
        String diseaseTable=getDiseaseTableHTML(annotatedDiseases,termID);
        String content = String.format(HTML_TEMPLATE,CSS, term.getName(),termID, definition, comment,  synonyms,diseaseTable);
        return content;
    }


    private static final String getDiseaseTableHTML(List<String> diseases,String Id) {
        if (diseases==null) { return "<p>No disease annotations found.</p>"; }
        String header=String.format(" <div class=\"container\">\n" +
                "  <table class=\"responsive-table\">\n" +
                "    <caption>Diseases annotated to %s</caption>\n" +
                "    <thead>\n" +
                "      <tr>\n" +
                "        <th scope=\"col\">Id</th>\n" +
                "        <th scope=\"col\">Disease</th>\n" +
                "      </tr>\n" +
                "    </thead>\n" +
                "    <tfoot>\n" +
                "      <tr>\n" +
                "        <td colspan=\"2\">More information: <a href=\"http://www.human-phenotype-ontology.org\">HPO Website</a></td>\n" +
                "      </tr>\n" +
                "    </tfoot>",Id);
        StringBuilder sb = new StringBuilder();
        for (String s : diseases) {
            String A[]=s.split("\\|");
            String row = String.format("<tr>\n" +
                    "        <th scope=\"row\">%s</th>\n" +
                    "        <td data-title=\"Id\">%s</td>\n" +
                    "      </tr>",A[0],A[1]);
            sb.append(row);
        }
        String table = String.format("%s<tbody>%s</tbody></table></div>",header,sb.toString());
        return table;


    }




    private static final String HTML_TEMPLATE = "<!DOCTYPE html>" +
            "<html lang=\"en\"><head>" +
            "<style>%s</style>\n" +
            "<meta charset=\"UTF-8\"><title>HPO tree browser</title></head>" +
            "<body>" +
            "<h1>%s</h1>" +
            "<p><b>ID:</b> %s</p>" +
            "<p><b>Definition:</b> %s</p>" +
            "<p><b>Comment:</b> %s</p>" +
            "<p><b>Synonyms:</b> %s</p>" +
            "%s"+
            "</body></html>";



    private static final String CSS="@import \"bourbon\";\n" +
            "\n" +
            "// Breakpoints\n" +
            "$bp-maggie: 15em; \n" +
            "$bp-lisa: 30em;\n" +
            "$bp-bart: 48em;\n" +
            "$bp-marge: 62em;\n" +
            "$bp-homer: 75em;\n" +
            "\n" +
            "// Styles\n" +
            "* {\n" +
            " @include box-sizing(border-box);\n" +
            " \n" +
            " &:before,\n" +
            " &:after {\n" +
            "   @include box-sizing(border-box);\n" +
            " }\n" +
            "}\n" +
            "\n" +
            "body {\n" +
            "  font-family: $helvetica;\n" +
            "  color: rgba(94,93,82,1);\n" +
            "}\n" +
            "\n" +
            "a {\n" +
            "  color: rgba(51,122,168,1);\n" +
            "  \n" +
            "  &:hover,\n" +
            "  &:focus {\n" +
            "    color: rgba(75,138,178,1); \n" +
            "  }\n" +
            "}\n" +
            "\n" +
            ".container {\n" +
            "  margin: 5% 3%;\n" +
            "  \n" +
            "  @media (min-width: $bp-bart) {\n" +
            "    margin: 2%; \n" +
            "  }\n" +
            "  \n" +
            "  @media (min-width: $bp-homer) {\n" +
            "    margin: 2em auto;\n" +
            "    max-width: $bp-homer;\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            ".responsive-table {\n" +
            "  width: 100%;\n" +
            "  margin-bottom: 1.5em;\n" +
            "  \n" +
            "  @media (min-width: $bp-bart) {\n" +
            "    font-size: .9em; \n" +
            "  }\n" +
            "  \n" +
            "  @media (min-width: $bp-marge) {\n" +
            "    font-size: 1em; \n" +
            "  }\n" +
            "  \n" +
            "  thead {\n" +
            "    // Accessibly hide <thead> on narrow viewports\n" +
            "    position: absolute;\n" +
            "    clip: rect(1px 1px 1px 1px); /* IE6, IE7 */\n" +
            "    clip: rect(1px, 1px, 1px, 1px);\n" +
            "    padding: 0;\n" +
            "    border: 0;\n" +
            "    height: 1px; \n" +
            "    width: 1px; \n" +
            "    overflow: hidden;\n" +
            "    \n" +
            "    @media (min-width: $bp-bart) {\n" +
            "      // Unhide <thead> on wide viewports\n" +
            "      position: relative;\n" +
            "      clip: auto;\n" +
            "      height: auto;\n" +
            "      width: auto;\n" +
            "      overflow: auto;\n" +
            "    }\n" +
            "    \n" +
            "    th {\n" +
            "      background-color: rgba(29,150,178,1);\n" +
            "      border: 1px solid rgba(29,150,178,1);\n" +
            "      font-weight: normal;\n" +
            "      text-align: left;\n" +
            "      color: white;\n" +
            "      \n" +
            "      &:first-of-type {\n" +
            "        text-align: left; \n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "  \n" +
            "  // Set these items to display: block for narrow viewports\n" +
            "  tbody,\n" +
            "  tr,\n" +
            "  th,\n" +
            "  td {\n" +
            "    display: block;\n" +
            "    padding: 0;\n" +
            "    text-align: left;\n" +
            "    white-space: normal;\n" +
            "  }\n" +
            "  \n" +
            "  tr {   \n" +
            "    @media (min-width: $bp-bart) {\n" +
            "      // Undo display: block \n" +
            "      display: table-row; \n" +
            "    }\n" +
            "  }\n" +
            "  \n" +
            "  th,\n" +
            "  td {\n" +
            "    padding: .5em;\n" +
            "    vertical-align: middle;\n" +
            "    \n" +
            "    @media (min-width: $bp-lisa) {\n" +
            "      padding: .75em .5em; \n" +
            "    }\n" +
            "    \n" +
            "    @media (min-width: $bp-bart) {\n" +
            "      // Undo display: block \n" +
            "      display: table-cell;\n" +
            "      padding: .5em;\n" +
            "    }\n" +
            "    \n" +
            "    @media (min-width: $bp-marge) {\n" +
            "      padding: .75em .5em; \n" +
            "    }\n" +
            "    \n" +
            "    @media (min-width: $bp-homer) {\n" +
            "      padding: .75em; \n" +
            "    }\n" +
            "  }\n" +
            "  \n" +
            "  caption {\n" +
            "    margin-bottom: 1em;\n" +
            "    font-size: 1em;\n" +
            "    font-weight: bold;\n" +
            "    text-align: left;\n" +
            "    \n" +
            "    @media (min-width: $bp-bart) {\n" +
            "      font-size: 1.5em;\n" +
            "    }\n" +
            "  }\n" +
            "  \n" +
            "  tfoot {\n" +
            "    font-size: .8em;\n" +
            "    font-style: italic;\n" +
            "    \n" +
            "    @media (min-width: $bp-marge) {\n" +
            "      font-size: .9em;\n" +
            "    }\n" +
            "  }\n" +
            "  \n" +
            "  tbody {\n" +
            "    @media (min-width: $bp-bart) {\n" +
            "      // Undo display: block \n" +
            "      display: table-row-group; \n" +
            "    }\n" +
            "    \n" +
            "    tr {\n" +
            "      margin-bottom: 1em;\n" +
            "      border: 2px solid rgba(29,150,178,1);\n" +
            "      \n" +
            "      @media (min-width: $bp-bart) {\n" +
            "        // Undo display: block \n" +
            "        display: table-row;\n" +
            "        border-width: 1px;\n" +
            "      }\n" +
            "      \n" +
            "      &:last-of-type {\n" +
            "        margin-bottom: 0; \n" +
            "      }\n" +
            "      \n" +
            "      &:nth-of-type(even) {\n" +
            "        @media (min-width: $bp-bart) {\n" +
            "          background-color: rgba(94,93,82,.1);\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "    \n" +
            "    th[scope=\"row\"] {\n" +
            "      background-color: rgba(29,150,178,1);\n" +
            "      color: white;\n" +
            "      \n" +
            "      @media (min-width: $bp-bart) {\n" +
            "        background-color: transparent;\n" +
            "        color: rgba(94,93,82,1);\n" +
            "        text-align: left;\n" +
            "      }\n" +
            "    }\n" +
            "    \n" +
            "    td {\n" +
            "      text-align: right;\n" +
            "      \n" +
            "      @media (min-width: $bp-lisa) {\n" +
            "        border-bottom: 1px solid  rgba(29,150,178,1);\n" +
            "      }\n" +
            "      \n" +
            "      @media (min-width: $bp-bart) {\n" +
            "        text-align: center; \n" +
            "      }\n" +
            "    }\n" +
            "    \n" +
            "    td[data-type=currency] {\n" +
            "      text-align: right; \n" +
            "    }\n" +
            "    \n" +
            "    td[data-title]:before {\n" +
            "      content: attr(data-title);\n" +
            "      float: left;\n" +
            "      font-size: .8em;\n" +
            "      color: rgba(94,93,82,.75);\n" +
            "      \n" +
            "      @media (min-width: $bp-lisa) {\n" +
            "        font-size: .9em; \n" +
            "      }\n" +
            "      \n" +
            "      @media (min-width: $bp-bart) {\n" +
            "        // Don’t show data-title labels \n" +
            "        content: none; \n" +
            "      }\n" +
            "    } \n" +
            "  }\n" +
            "}";



}
