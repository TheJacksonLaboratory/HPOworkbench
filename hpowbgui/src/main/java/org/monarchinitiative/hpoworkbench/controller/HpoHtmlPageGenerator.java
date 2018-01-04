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
        for (String s : diseases) {
            String A[]=s.split("\\|");
            String row = String.format("<tr>\n" +
                    "        <td>%s</td>\n" +
                    "        <td>%s</td>\n" +
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
