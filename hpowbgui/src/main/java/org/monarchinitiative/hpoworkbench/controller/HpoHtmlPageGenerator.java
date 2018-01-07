package org.monarchinitiative.hpoworkbench.controller;

import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.ontology.data.TermSynonym;
import org.monarchinitiative.hpoworkbench.model.DiseaseModel;

import java.util.List;
import java.util.stream.Collectors;
/**
 * Class that generates the HTML code for the WebView that shows either the HPO terms and the list of
 * diseases annotation to them or a disease and all of the HPO terms annotated to it.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
class HpoHtmlPageGenerator {

     static String getHTML(HpoTerm term, List<DiseaseModel> annotatedDiseases) {

        String termID = term.getId().getIdWithPrefix();
        String synonyms = (term.getSynonyms() == null) ? "" : term.getSynonyms().stream().map(TermSynonym::getValue)
                .collect(Collectors.joining("; "));
        String definition = (term.getDefinition() == null) ? "" : term.getDefinition();
        String comment = (term.getComment() == null) ? "-" : term.getComment();
        String diseaseTable=getDiseaseTableHTML(annotatedDiseases,termID);
        return String.format(HTML_TEMPLATE,CSS, term.getName(),termID, definition, comment,  synonyms,diseaseTable);
    }

    /**
     * Produce HTML for for the list of all disease to which an HPO term is annotated.
     * @param diseases
     * @param Id
     * @return
     */
    private static String getDiseaseTableHTML(List<DiseaseModel> diseases,String Id) {
        if (diseases==null) { return "<p>No disease annotations found.</p>"; }
        String header=String.format("\n" +
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
                "    </tfoot>",Id,diseases.size());
        StringBuilder sb = new StringBuilder();
        for (DiseaseModel s : diseases) {
            String row = String.format("<tr>\n" +
                    "        <td>%s</td>\n" +
                    "        <td>%s</td>\n" +
                    "      </tr>",s.getDiseaseDbAndId(),s.getDiseaseName());
            sb.append(row);
        }
        return String.format("%s<tbody>%s</tbody></table></div>",header,sb.toString());
    }


     static String getDiseaseHTML(String database, String name, List<HpoTerm> terms) {
        String listOfTerms=getListOfTermsHTML(terms);

        return String.format(DISEASE_TEMPLATE,CSS,name, database,name,listOfTerms );
    }


    private static String getListOfTermsHTML(List<HpoTerm> terms) {
        if (terms==null) { return "<p>No HPO annotations found.</p>"; }
        String header=String.format("\n" +
                "  <table class=\"zebra\">\n" +
                "    <caption  style=\"color:#222;text-shadow:0px 1px 2px #555;font-size:24px;\">HPO Terms (n=%d)</caption>\n" +
                "    <thead>\n" +
                "      <tr>\n" +
                "        <th>Id</th>\n" +
                "        <th>Label</th>\n" +
                "        <th>Definition</th>\n" +
                "      </tr>\n" +
                "    </thead>\n" +
                "    <tfoot>\n" +
                "      <tr>\n" +
                "        <td colspan=\"3\">More information: <a href=\"http://www.human-phenotype-ontology.org\">HPO Website</a></td>\n" +
                "      </tr>\n" +
                "    </tfoot>",terms.size());
        StringBuilder sb = new StringBuilder();
        for (HpoTerm term : terms) {
            String row = String.format("<tr>\n" +
                    "        <td>%s</td>\n" +
                    "        <td>%s</td>\n" +
                    "        <td>%s</td>\n" +
                    "      </tr>",term.getId().getIdWithPrefix(),
                    term.getName(),
                    term.getDefinition()!=null?term.getDefinition():"");
            sb.append(row);
        }
        return String.format("%s<tbody>%s</tbody></table></div>",header,sb.toString());
    }




    private static final String DISEASE_TEMPLATE ="<!DOCTYPE html>" +
            "<html lang=\"en\"><head>" +
            "<style>%s</style>\n" +
            "<meta charset=\"UTF-8\"><title>HPO disease browser</title></head>" +
            "<body>" +
            "<h1>%s</h1>" +
            "<p><b>Disease ID:</b> %s</p>" +
            "<p><b>Name:</b> %s</p>" +
            "%s"+
            "</body></html>";

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



    private static final String CSS="body {\n" +
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
