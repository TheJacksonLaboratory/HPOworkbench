package org.monarchinitiative.hpoworkbench.controller;


import com.google.common.collect.ImmutableList;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoOnset;
import org.monarchinitiative.phenol.annotations.formats.hpo.category.HpoCategory;
import org.monarchinitiative.phenol.annotations.formats.hpo.category.HpoCategoryMap;
import org.monarchinitiative.phenol.ontology.data.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class that generates the HTML code for the WebView that shows either the HPO terms and the list of
 * diseases annotated to them or a disease and all of the HPO terms annotated to it.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
class HpoHtmlPageGenerator {
    /**@return A String with the HTML for representing one HPO term and the diseases it is annotated to. */
    static String getHTML(Term term, List<HpoDisease> annotatedDiseases) {

        String termID = term.getId().getValue();
        String synonyms = (term.getSynonyms() == null) ? "" : term.getSynonyms().stream().map(TermSynonym::getValue)
                .collect(Collectors.joining("; "));
        String definition = (term.getDefinition() == null) ? "" : term.getDefinition();
        String comment = (term.getComment() == null) ? "-" : term.getComment();
        String diseaseTable = getDiseaseTableHTML(annotatedDiseases, termID);
        List<SimpleXref> pmids=term.getPmidXrefs();
        String pmidList;
        if (pmids.isEmpty())
            pmidList="-";
        else
        pmidList= pmids.stream().map(SimpleXref::getCurie).collect(Collectors.joining(": "));
        return String.format(HTML_TEMPLATE, CSS, term.getName(), termID, definition, comment, synonyms, pmidList,diseaseTable);
    }

    /**
     * Produce HTML for for the list of all disease to which an HPO term is annotated.
     *
     * @param diseases All of the diseases to which the HPO term is annotated
     * @param Id ID of the HPO term in question
     * @return String to be displayed in an HTML browser
     */
    private static String getDiseaseTableHTML(List<HpoDisease> diseases, String Id) {
        if (diseases == null) {
            return "<p>No disease annotations found.</p>";
        }
        String header = String.format("\n" +
                "  <table class=\"zebra\">\n" +
                "    <caption  style=\"color:#222;text-shadow:0px 1px 2px #555;font-size:24px;\">Diseases annotated to %s (n=%d)</caption>\n" +
                "    <thead>\n" +
                "      <tr>\n" +
                "        <th>Id</th>\n" +
                "        <th>Disease</th>\n" +
                "      </tr>\n" +
                "    </thead>\n" +
                "    <tfoot>\n" +
                "      <tr>\n" +
                "        <td colspan=\"2\">More information: <a href=\"http://www.human-phenotype-ontology.org\">HPO Website</a></td>\n" +
                "      </tr>\n" +
                "    </tfoot>", Id, diseases.size());
        StringBuilder sb = new StringBuilder();
        for (HpoDisease s : diseases) {
            String row = String.format("<tr>\n" +
                    "        <td><a href=\"%s\">%s</a></td>\n" +
                    "        <td>%s</td>\n" +
                    "      </tr>", s.getName(), s.getDiseaseDatabaseId().getValue(), s.getName());
            sb.append(row);
        }
        return String.format("%s<tbody>%s</tbody></table></div>", header, sb);
    }


    private static final String DISEASE_TEMPLATE = "<!DOCTYPE html>" +
            "<html lang=\"en\"><head>" +
            "<style>%s</style>\n" +
            "<meta charset=\"UTF-8\"><title>HPO disease browser</title></head>" +
            "<body>" +
            "%s" +
            "</body></html>";


    static String getDiseaseHTML(HpoDisease disease, Ontology ontology) {
        String listOfCategories = getListOfTermsHTML(disease, ontology);
        return String.format(DISEASE_TEMPLATE, CSS,  listOfCategories);
    }


    private static List<Term> getTerms(List<TermId> ids,Ontology ontology) {
        ImmutableList.Builder<Term> builder = new ImmutableList.Builder<>();
        for (TermId tid : ids){
            Term term = ontology.getTermMap().get(tid);
            if (term==null) {
                System.err.println("[WARNING] Null term for " + tid.getValue());
                continue;
            }
            builder.add(term);
        }
        return builder.build();
    }

    private static List<String> getTermsNamesFromIds(List<TermId> tids, Ontology ontology) {
        ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();
        for (TermId id : tids) {
            Term t = ontology.getTermMap().get(id);
            builder.add(t.getName());
        }
        return builder.build();
    }

    /** @return String representing an HTML table row for one disease annotation. */
    private static String getAnnotationTableRow(HpoAnnotation annot, Ontology ontology) {
        TermId tid = annot.getTermId();
        Term term = ontology.getTermMap().get(tid);
        String label = term.getName();
        String definition = term.getDefinition() != null ? term.getDefinition() : "";
        // try to get whatever we have in terms of frequency or modifiers
        String fr = String.format("Frequency=%s",annot.getFrequencyString());
        List<TermId> modifiers = annot.getModifiers();
        HpoOnset onset = annot.getOnset();
        StringBuilder sb = new StringBuilder();
        sb.append(fr);
        if (modifiers.size()>0) {
            List<String> names=getTermsNamesFromIds(modifiers,ontology);
            sb.append("</br>Modifiers: ").append(String.join("; ",names));
        }
        if (onset.available()) {
            sb.append("</br>").append(onset);
        }
        sb.append("</br>Source: ").append(String.join("; ",annot.getCitations()));
        return String.format("<tr>\n" +
                        "        <td><a href=\"%s\">%s</a></td>\n" +
                        "        <td>%s</td>\n" +
                        "        <td>%s</td>\n" +
                        "        <td>%s</td>\n" +
                        "      </tr>\n",
                term.getId().getValue(),
                term.getId().getValue(),
                label,
                definition,
                sb);
    }



    /**
     * Create a table with the HPO Categories and annotations.
     */
    private static String getListOfTermsHTML(HpoDisease disease, Ontology ontology) {
        List<Term> modesOfInheritance = getTerms(disease.getModesOfInheritance(),ontology);
        List<Term> negativeTerms=getTerms(disease.getNegativeAnnotations(),ontology);
        List<HpoAnnotation> annotations = disease.getPhenotypicAbnormalities();

        if (annotations == null) {
            return "<p>No HPO annotations found.</p>";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>").append(disease.getName()).append("</h1>\n");
        sb.append("<p><b>Disease ID:</b>").append(disease.getDiseaseDatabaseId()).append("<br/>");
        sb.append("<b>Name:</b>").append(disease.getName()).append("<br/>");
        // output inheritance
        String inheritanceString="No mode of inheritance information available";
        if (modesOfInheritance.size()>0) {
            inheritanceString=modesOfInheritance.stream().map(Term::getName).collect(Collectors.joining("; "));
        }
        sb.append("<b>Inheritance:</b>").append(inheritanceString).append("<br/>");
        sb.append("<b>Number of annotations:</b>").append(annotations.size()).append("</p>\n");
        HpoCategoryMap hpocatmap = new HpoCategoryMap();
        Map<TermId,HpoAnnotation> id2annotationmap=new HashMap<>();
        for (HpoAnnotation annot : annotations) {
            TermId tid = annot.getTermId();
            hpocatmap.addAnnotatedTerm(tid, ontology);
            id2annotationmap.put(tid,annot);
        }
        List<HpoCategory> hpocatlist = hpocatmap.getActiveCategoryList();

        for (HpoCategory cat : hpocatlist) {
            String template=cat.getNumberOfAnnotations()>1?"%s (%d annotations)":"%s (%d annotation)";
            String title = String.format(template, cat.getLabel(), cat.getNumberOfAnnotations());
            sb.append(String.format("  <table class=\"zebra\">\n" +
                    "    <caption  style=\"color:#222;text-shadow:0px 1px 2px #555;font-size:24px;\">%s</caption>\n" +
                    "    <thead>\n" +
                    "      <tr>\n" +
                    "        <th>Id</th><th>Label</th><th>Definition</th><th>Other information</th>\n" +
                    "      </tr>\n" +
                    "    </thead>\n", title));
            List<TermId> termIdList = cat.getAnnotatingTermIds();
            for (TermId tid : termIdList) {
                HpoAnnotation annot = id2annotationmap.get(tid);
                sb.append(getAnnotationTableRow(annot,ontology));
            }
            sb.append("\n");
        }
        if (negativeTerms.size()>0) {
            sb.append("<h2>Features that are not observed in this disease</h2><ol>");
            for (Term term : negativeTerms) {
                sb.append("<li>").append(term.getName()).append("</li>\n");
            }
            sb.append("</ol>");
        }


        return sb.toString();
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
            "<p><b>PMID:</b> %s</p>" +
            "%s" +
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
            "tbody tr:nth-child(odd) {\n" +
            "  background: #eee;\n" +
            "}";


}
