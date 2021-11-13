package org.monarchinitiative.hpoworkbench.html;

import org.monarchinitiative.hpoworkbench.analysis.AnnotationTlc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AnnotationTlcHtmlGenerator {

    private static final String HTML_TEMPLATE = "<!DOCTYPE html>" +
            "<html lang=\"en\"><head>" +
            "<style>%s</style>\n" +
            "<meta charset=\"UTF-8\"><title>Human Phenotype Ontology: Diseases likely in need of better annotations</title></head>" +
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




    public static String getNewAnnotationHTML(LocalDate d,
                                              int annot_count,
                                              int newannot_count,
                                              int oldannotcount,
                                              Map<LocalDate,Integer> counter){
        final String HTML_TEMPLATE = "<!DOCTYPE html>" +
                "<html lang=\"en\"><head>" +
                "<style>%s</style>\n" +
                "<meta charset=\"UTF-8\"><title>Human Phenotype Ontology</title></head>" +
                "<body><h1>New annotations</h1>";
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_TEMPLATE)
                .append("<p>These are the annotations counts since ").append(d.toString()).append("</p>");
        sb.append("<ul><li>Total annotations: ").append(annot_count).append("</li>")
                .append("<li>New annotations: ").append(newannot_count).append("</li>")
                .append("<li>Old annotations: ").append(oldannotcount).append("</li>")
                .append("</ul>")
                .append("<p>Here is the count of annotations per day</p><ul>");

        List<LocalDate> dates = new ArrayList<>(counter.keySet());
        Collections.sort(dates);

        for (LocalDate s : dates) {
            int c = counter.get(s);
            sb.append("<li>").append(s.toString()).append(": ").append(c).append("</li>");
        }
        sb.append( "</ul></body></html>");
        return sb.toString();
    }



}
