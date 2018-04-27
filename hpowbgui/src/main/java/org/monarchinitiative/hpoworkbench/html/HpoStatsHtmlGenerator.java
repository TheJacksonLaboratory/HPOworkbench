package org.monarchinitiative.hpoworkbench.html;

import org.monarchinitiative.hpoworkbench.analysis.HpoStats;

public class HpoStatsHtmlGenerator {



    private static final String HTML_TEMPLATE = "<!DOCTYPE html>" +
            "<html lang=\"en\"><head>" +
            "<style>%s</style>\n" +
            "<meta charset=\"UTF-8\"><title>Human Phenotype Ontology: Term counts</title></head>" +
            "<body>" +
            "%s \n" + // the Mondo , summary, and the annotation HTML code go here
            "</body></html>";

    private static final String CSS = "body {\n" +
            "  font: normal medium/1.4 sans-serif;\n" +
            "}\n" +
            "table {\n" +
            "  border-collapse: collapse;\n" +
            "  width: 100%;\n" +
            "}\n" +
            "th, td {\n" +
            "  padding: 0.25rem;\n" +
            "  text-align: left;\n" +
            "  border: 1px solid #ccc;\n" +
            "}\n" +
            "tr.myheader {background:#fff} "+
            "tr.shared { background:#3ff} " +
            "tr.subclazz { background:#f3f} " +
            "tr.unrelated { background:#ff3} " +
            // "tbody tr:nth-child(odd) {\n" +
            //"  background: #eee;\n" +
            "}";



    public static String getHTML(HpoStats hpostats) {

        return String.format(HTML_TEMPLATE,CSS,getCounts(hpostats));
    }



    private static String getCounts(HpoStats hpostats) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Statistics for HPO starting at term ").append(hpostats.getHpoTerm()).append("</h1>");
        sb.append("<p>Term definition: ").append(hpostats.getHpoDefinition()).append("</p>");
        sb.append("<ol>");
        sb.append("<li>Number of terms: ").append(hpostats.getN_terms()).append("</li>");

        sb.append("</ol>");
        return sb.toString();
    }

}
