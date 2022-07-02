package org.monarchinitiative.hpoworkbench.controller;


import com.google.common.collect.ImmutableList;
import org.monarchinitiative.phenol.annotations.base.temporal.Age;
import org.monarchinitiative.phenol.annotations.formats.hpo.*;
import org.monarchinitiative.phenol.annotations.formats.hpo.category.HpoCategory;
import org.monarchinitiative.phenol.annotations.formats.hpo.category.HpoCategoryMap;
import org.monarchinitiative.phenol.ontology.data.*;


import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that generates the HTML code for the WebView that shows either the HPO terms and the list of
 * diseases annotated to them or a disease and all of the HPO terms annotated to it.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
class HpoHtmlPageGenerator {
    /**
     * @return A String with the HTML for representing one HPO term and the diseases it is annotated to.
     */
    static String getHTML(Term term, List<HpoDisease> annotatedDiseases, int n_descendents) {

        String termID = term.id().getValue();
        String synonyms = (term.getSynonyms() == null) ? "" : term.getSynonyms().stream().map(TermSynonym::getValue)
                .collect(Collectors.joining("; "));
        String definition = (term.getDefinition() == null) ? "" : term.getDefinition();
        String comment = (term.getComment() == null) ? "-" : term.getComment();
        String diseaseTable = getDiseaseTableHTML(annotatedDiseases, termID);
        List<SimpleXref> pmids = term.getPmidXrefs();
        String pmidList;
        if (pmids.isEmpty())
            pmidList = "-";
        else
            pmidList = pmids.stream().map(SimpleXref::getCurie).collect(Collectors.joining(": "));
        return String.format(HTML_TEMPLATE, CSS, term.getName(), termID, definition, comment, synonyms, pmidList, n_descendents, diseaseTable);
    }

    /**
     * Produce HTML for for the list of all disease to which an HPO term is annotated.
     *
     * @param diseases All of the diseases to which the HPO term is annotated
     * @param Id       ID of the HPO term in question
     * @return String to be displayed in an HTML browser
     */
    private static String getDiseaseTableHTML(List<HpoDisease> diseases, String Id) {
        if (diseases == null) {
            return "<p>No disease annotations found.</p>";
        }
        String header = String.format("""

                <table class="zebra">
                  <caption  style="color:#222;text-shadow:0px 1px 2px #555;font-size:24px;">Diseases annotated to %s (n=%d)</caption>
                  <thead>
                    <tr>
                      <th>Id</th>
                      <th>Disease</th>
                    </tr>
                  </thead>
                  <tfoot>
                    <tr>
                      <td colspan="2">More information: <a href="https://hpo.jax.org">HPO Website</a></td>
                    </tr>
                  </tfoot>""".indent(2), Id, diseases.size());
        StringBuilder sb = new StringBuilder();
        for (HpoDisease disease : diseases) {
            String row = String.format("""
                             <tr>
                                <td><a href="https://hpo.jax.org/app/browse/disease/%s">%s</a></td>
                                <td>%s</td>
                            </tr>""",
                    disease.id().getValue(),
                    disease.diseaseName(),
                    disease.diseaseName());
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
        return String.format(DISEASE_TEMPLATE, CSS, listOfCategories);
    }


    private static List<Term> getTerms(List<TermId> ids, Ontology ontology) {
        ImmutableList.Builder<Term> builder = new ImmutableList.Builder<>();
        for (TermId tid : ids) {
            Term term = ontology.getTermMap().get(tid);
            if (term == null) {
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

    /**
     * @return String representing an HTML table row for one disease annotation.
     */
    private static String getAnnotationTableRow(HpoDiseaseAnnotation annot, Ontology ontology) {
        TermId tid = annot.id();
        Term term = ontology.getTermMap().get(tid);
        String label = term.getName();
        String definition = term.getDefinition() != null ? term.getDefinition() : "";
        // try to get whatever we have in terms of frequency or modifiers
        String fr = String.format("Frequency=%.1f%%", annot.frequency());
        List<TermId> modifiers = annot.metadata()
                .map(HpoDiseaseAnnotationMetadata::modifiers)
                .flatMap(Collection::stream)
                .toList();
        Optional<Age> onset = annot.earliestOnset();
        StringBuilder sb = new StringBuilder();
        sb.append(fr);
        if (modifiers.size() > 0) {
            List<String> names = getTermsNamesFromIds(modifiers, ontology);
            sb.append("</br>Modifiers: ").append(String.join("; ", names));
        }
        if (onset.isPresent()) {
            sb.append("</br>").append(onset);
        }
        //  sb.append("</br>Source: ").append(String.join("; ",annot.metadata().map(HpoDiseaseAnnotationMetadata::)));
        sb.append("</br>Source: TODO");
        return String.format("""
                        <tr>
                          <td><a href="https://hpo.jax.org/app/browse/term/%s">%s</a></td>
                          <td>%s</td>
                          <td>%s</td>
                          <td>%s</td>
                        </tr>
                        """,
                term.id().getValue(),
                term.id().getValue(),
                label,
                definition,
                sb);
    }


    /**
     * Create a table with the HPO Categories and annotations.
     */
    private static String getListOfTermsHTML(HpoDisease disease, Ontology ontology) {
        List<Term> modesOfInheritance = getTerms(disease.modesOfInheritance(), ontology);
        //List<Term> negativeTerms=getTerms(disease.getNegativeAnnotations(),ontology);

        if (disease.annotationCount() == 0) {
            return "<p>No HPO annotations found.</p>";
        }
        StringBuilder sb = new StringBuilder();


        sb.append("<h1>").append(disease.diseaseName()).append("</h1>\n");
        sb.append("<p><b>Disease ID:</b>").append(disease.id().getValue()).append("<br/>");
        sb.append("<b>Name:</b>").append(disease.diseaseName()).append("<br/>");
        // output inheritance
        String inheritanceString = "No mode of inheritance information available";
        if (modesOfInheritance.size() > 0) {
            inheritanceString = modesOfInheritance.stream().map(Term::getName).collect(Collectors.joining("; "));
        }
        sb.append("<b>Inheritance:</b>").append(inheritanceString).append("<br/>");
        sb.append("<b>Number of annotations:</b>").append(disease.annotationCount()).append("</p>\n");
        HpoCategoryMap hpocatmap = new HpoCategoryMap();
        Map<TermId, HpoDiseaseAnnotation> id2annotationmap = new HashMap<>();
        for (HpoDiseaseAnnotation annotation : disease.annotations()) {
            TermId tid = annotation.id();
            hpocatmap.addAnnotatedTerm(tid, ontology);
            id2annotationmap.put(tid, annotation);
        }
        List<HpoCategory> hpocatlist = hpocatmap.getActiveCategoryList();

        for (HpoCategory cat : hpocatlist) {
            String template = cat.getNumberOfAnnotations() > 1 ? "%s (%d annotations)" : "%s (%d annotation)";
            String title = String.format(template, cat.getLabel(), cat.getNumberOfAnnotations());
            sb.append(String.format("""
                      <table class="zebra">
                        <caption  style="color:#222;text-shadow:0px 1px 2px #555;font-size:24px;">%s</caption>
                        <thead>
                          <tr>
                            <th>Id</th><th>Label</th><th>Definition</th><th>Other information</th>
                          </tr>
                        </thead>
                    """, title));
            List<TermId> termIdList = cat.getAnnotatingTermIds();
            for (TermId tid : termIdList) {
                HpoDiseaseAnnotation annot = id2annotationmap.get(tid);
                sb.append(getAnnotationTableRow(annot, ontology));
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
            "<p><b>PMID:</b> %s</p>" +
            "<p><b>Descendants:</b> %d</p>" +
            "%s" +
            "</body></html>";


    private static final String CSS = """
            body {
              font: normal medium/1.4 sans-serif;
            }
            table {
              border-collapse: collapse;
              width: 100%;
            }
            th, td {
              padding: 0.25rem;
              text-align: left;
              border: 1px solid #ccc;
            }
            tbody tr:nth-child(odd) {
              background: #eee;
            }""";


}
