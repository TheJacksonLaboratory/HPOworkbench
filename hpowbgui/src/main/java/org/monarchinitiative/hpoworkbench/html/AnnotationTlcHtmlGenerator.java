package org.monarchinitiative.hpoworkbench.html;

import org.monarchinitiative.hpoworkbench.analysis.AnnotationTlc;

public class AnnotationTlcHtmlGenerator {

    private static final String HTML_TEMPLATE = "<!DOCTYPE html>" +
            "<html lang=\"en\"><head>" +
            "<style>%s</style>\n" +
            "<meta charset=\"UTF-8\"><title>Human Phenotype Ontology: Diseases liekly in need of better annotations</title></head>" +
            "<body>" +
            "%s \n" + // the Mondo , summary, and the annotation HTML code go here
            "</body></html>";


    public static String getHTML(AnnotationTlc tlc) {
        return String.format(HTML_TEMPLATE,Css.getCSS(),getAnnotationTlc(tlc));
    }


    public static String getHTMLSpecificTerms(AnnotationTlc tlc) {
        return String.format(HTML_TEMPLATE,Css.getCSS(),getAnnotationTlc2(tlc));
    }

    private static String getAnnotationTlc(AnnotationTlc tlc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Entries that might need better annotations</h1>");
        sb.append("<p>Entries with less than three annotations</p>");
        sb.append("<ol>");

        for (String key : tlc.getUnderannotatedDiseases().keySet()) {
            sb.append("<li>").append(key).append(": ").append(tlc.getUnderannotatedDiseases().get(key)).append("</li>");
        }
        sb.append("</ol>");
        return sb.toString();
    }

    private static String getAnnotationTlc2(AnnotationTlc tlc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Entries that might need better annotations</h1>");
        sb.append("<p>Entries with unspecific annotations</p>");
        sb.append("<ol>");

        for (String key : tlc.getDiseasesWithTooGeneralAnnotations().keySet()) {
            sb.append("<li>").append(key).append(": ").append(tlc.getDiseasesWithTooGeneralAnnotations().get(key)).append("</li>");
        }
        sb.append("</ol>");
        return sb.toString();
    }
}
