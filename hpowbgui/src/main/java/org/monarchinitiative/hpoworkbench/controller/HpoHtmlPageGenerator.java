package org.monarchinitiative.hpoworkbench.controller;


import org.monarchinitiative.hpoworkbench.model.DiseaseModel;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.category.HpoCategory;
import org.monarchinitiative.phenol.formats.hpo.category.HpoCategoryMap;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermSynonym;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class that generates the HTML code for the WebView that shows either the HPO terms and the list of
 * diseases annotation to them or a disease and all of the HPO terms annotated to it.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
class HpoHtmlPageGenerator {

    static String getHTML(Term term, List<DiseaseModel> annotatedDiseases) {

        String termID = term.getId().getIdWithPrefix();
        String synonyms = (term.getSynonyms() == null) ? "" : term.getSynonyms().stream().map(TermSynonym::getValue)
                .collect(Collectors.joining("; "));
        String definition = (term.getDefinition() == null) ? "" : term.getDefinition();
        String comment = (term.getComment() == null) ? "-" : term.getComment();
        String diseaseTable = getDiseaseTableHTML(annotatedDiseases, termID);
        return String.format(HTML_TEMPLATE, CSS, term.getName(), termID, definition, comment, synonyms, diseaseTable);
    }

    /**
     * Produce HTML for for the list of all disease to which an HPO term is annotated.
     *
     * @param diseases All of the diseases to which the HPO term is annotated
     * @param Id ID of the HPO term in question
     * @return String to be displayed in an HTML browser
     */
    private static String getDiseaseTableHTML(List<DiseaseModel> diseases, String Id) {
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
        for (DiseaseModel s : diseases) {
            String row = String.format("<tr>\n" +
                    "        <td><a href=\"%s\">%s</a></td>\n" +
                    "        <td>%s</td>\n" +
                    "      </tr>", s.getDiseaseName(), s.getDiseaseDbAndId(), s.getDiseaseName());
            sb.append(row);
        }
        return String.format("%s<tbody>%s</tbody></table></div>", header, sb.toString());
    }


    private static final String DISEASE_TEMPLATE = "<!DOCTYPE html>" +
            "<html lang=\"en\"><head>" +
            "<style>%s</style>\n" +
            "<meta charset=\"UTF-8\"><title>HPO disease browser</title></head>" +
            "<body>" +
            "<h1>%s</h1>" +
            "<p><b>Disease ID:</b> %s<br/>" +
            "<b>Name:</b> %s<br/>" +
            "<b>Number of annotations:</b> %d</p>\n" +
            "%s" +
            "</body></html>";


    static String getDiseaseHTML(String database, String name, List<Term> terms, HpoOntology ontology) {
        String listOfCategories = getListOfTermsHTML(terms, ontology);
        int n_annotations = terms.size();
        return String.format(DISEASE_TEMPLATE, CSS, name, database, name, n_annotations, listOfCategories);
    }

    /**
     * Create a table with the HPO Categories and annotations.
     */
    private static String getListOfTermsHTML(List<Term> terms, HpoOntology ontology) {
        if (terms == null) {
            return "<p>No HPO annotations found.</p>";
        }
        HpoCategoryMap hpocatmap = new HpoCategoryMap();
        for (Term term : terms) {
            TermId tid = term.getId();
            hpocatmap.addAnnotatedTerm(tid, ontology);
        }
        List<HpoCategory> hpocatlist = hpocatmap.getActiveCategoryList();
        StringBuilder sb = new StringBuilder();
        for (HpoCategory cat : hpocatlist) {
            String template=cat.getNumberOfAnnotations()>1?"%s (%d annotations)":"%s (%d annotation)";
            String title = String.format(template, cat.getLabel(), cat.getNumberOfAnnotations());
            sb.append(String.format("  <table class=\"zebra\">\n" +
                    "    <caption  style=\"color:#222;text-shadow:0px 1px 2px #555;font-size:24px;\">%s</caption>\n" +
                    "    <thead>\n" +
                    "      <tr>\n" +
                    "        <th>Id</th><th>Label</th><th>Definition</th>\n" +
                    "      </tr>\n" +
                    "    </thead>\n" +
                    "    <tfoot>\n" +
                    "      <tr>\n" +
                    "        <td colspan=\"3\">More information: <a href=\"http://www.human-phenotype-ontology.org\">HPO Website</a></td>\n" +
                    "      </tr>\n" +
                    "    </tfoot>", title));
            List<TermId> termIdList = cat.getAnnotatingTermIds();
            for (TermId tid : termIdList) {
                Term term = ontology.getTermMap().get(tid);
                String row = String.format("<tr>\n" +
                                "        <td><a href=\"%s\">%s</a></td>\n" +
                                "        <td>%s</td>\n" +
                                "        <td>%s</td>\n" +
                                "      </tr>\n",
                        term.getId().getIdWithPrefix(),
                        term.getId().getIdWithPrefix(),
                        term.getName(),
                        term.getDefinition() != null ? term.getDefinition() : "");
                sb.append(row);
            }
            sb.append("\n");
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
