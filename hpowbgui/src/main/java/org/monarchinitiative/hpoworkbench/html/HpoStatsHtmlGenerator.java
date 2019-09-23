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


    public static String getHTML(HpoStats hpostats) {
        return String.format(HTML_TEMPLATE,Css.getCSS(),getCounts(hpostats));
    }


    private static String getAnnotationCountLines(int n, int n_annot, String database) {
        double mean = (double)n_annot/n;
        return String.format("<li>Number of  %s entries: %d</li>\n"+
                "<li>Number of annotations for %s entries: %d (%.1f per entry)</li>",database,n,database,n_annot,mean);
    }

    private static String getCounts(HpoStats hpostats) {
        StringBuilder sb = new StringBuilder();

        sb.append("<h1>HPO Statistics</h1>");
        sb.append("<ol>");
        sb.append("<li>Number of terms: ").append(hpostats.getN_terms()).append("</li>");
        sb.append("<li>Number of terms in the clinical course subontology: ").append(hpostats.getN_clinicalCourse()).append("</li>");
        sb.append("<li>Number of terms in the clinical modifier subontology: ").append(hpostats.getN_clinicalModifier()).append("</li>");
        sb.append("<li>Number of terms in the frequency subontology: ").append(hpostats.getN_frequency()).append("</li>");
        sb.append("<li>Number of terms in the mode of inheritance subontology: ").append(hpostats.getN_modeOfInheritance()).append("</li>");
        sb.append("<li>Number of terms in the phenotypic abnormality subontology: ").append(hpostats.getN_phenotypicAbnormality()).append("</li>");
        sb.append("<li>Number of terms with a textual definition: ").append(hpostats.getN_textual_def()).append("</li>");
        sb.append("<li>Number of synonyms: ").append(hpostats.getN_synonyms()).append("</li>");
        sb.append(getAnnotationCountLines(hpostats.getN_omim(),hpostats.getN_omim_annotations(),"OMIM"));
        sb.append(getAnnotationCountLines(hpostats.getN_orphanet(),hpostats.getN_orphanet_annotations(),"Orphanet"));
        sb.append(getAnnotationCountLines(hpostats.getN_decipher(),hpostats.getN_decipher_annotations(),"DECIPHER"));
        sb.append("<li>Total annotation count: ").append(hpostats.getTotalAnnotationCount()).append("</li>");
        sb.append("<li>Number of  relations: ").append(hpostats.getN_relations()).append("</li>");
        sb.append("<li>Number of  obsolete terms: ").append(hpostats.getN_obsolete()).append("</li>");
        sb.append("<li>Number of  non-obsolete terms: ").append(hpostats.getN_non_obsolete()).append("</li>");
        sb.append("<li>Number of  negated annotations: ").append(hpostats.getNegatedAnnotationCount()).append("</li>");
        for (String key : hpostats.getMetadata().keySet()) {
            sb.append("<li>").append(key).append(": ").append(hpostats.getMetadata().get(key)).append("</li>");
        }
        sb.append("</ol>");
        return sb.toString();
    }

}
