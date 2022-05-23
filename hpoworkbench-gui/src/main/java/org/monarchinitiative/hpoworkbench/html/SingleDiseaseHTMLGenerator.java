package org.monarchinitiative.hpoworkbench.html;


import org.monarchinitiative.phenol.annotations.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseaseAnnotation;
import org.monarchinitiative.phenol.annotations.formats.hpo.category.HpoCategory;
import org.monarchinitiative.phenol.annotations.formats.hpo.category.HpoCategoryMap;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.List;

/**
 * This is a convenience class that generates HTML code for Mondo diseases for which we can find just OMIM or just
 * Orphanet but not both annotations.
 * @author <a href="mailto:peter.robinson@jax.org:>Peter Robinson</a>
 */
public class SingleDiseaseHTMLGenerator {


    /**
     *
     * @param disease Object with HPO annotations for the disease
     * @param ontology reference to HPO ontology object
     * @return HTML with tables representing a disease that has only OMIM or Orphanet annotations but not both
     */
    public static String getHTML(HpoDisease disease, Ontology ontology) {
        return getListOfTermsHTML(disease,ontology);
    }




    /**
     * Create a table with the HPO Categories and annotations.
     */
    private static String getListOfTermsHTML(HpoDisease disease, Ontology ontology) {
        if (disease.phenotypicAbnormalitiesCount() == 0) {
            return "<p>No HPO annotations found.</p>";
        }
        HpoCategoryMap hpocatmap = new HpoCategoryMap();
        while (disease.phenotypicAbnormalities().hasNext()) {
            HpoDiseaseAnnotation annotation = disease.phenotypicAbnormalities().next();
            TermId tid = annotation.id();
            hpocatmap.addAnnotatedTerm(tid, ontology);

        }
        List<HpoCategory> hpocatlist = hpocatmap.getActiveCategoryList();
        StringBuilder sb = new StringBuilder();
        for (HpoCategory cat : hpocatlist) {
            String template=cat.getNumberOfAnnotations()>1?"%s (%d annotations)":"%s (%d annotation)";
            String title = String.format(template, cat.getLabel(), cat.getNumberOfAnnotations());
            sb.append(String.format("""
                    <table class="zebra">
                        <caption  style="color:#222;text-shadow:0px 1px 2px #555;font-size:24px;">%s</caption>
                        <thead>
                          <tr>
                            <th>Id</th><th>Label</th><th>Definition</th>
                          </tr>
                        </thead>
                        <tfoot>
                          <tr>
                            <td colspan="3">More information: <a href="http://www.human-phenotype-ontology.org">HPO Website</a></td>
                          </tr>
                        </tfoot><br/>""", title));
            List<TermId> termIdList = cat.getAnnotatingTermIds();
            for (TermId tid : termIdList) {
                Term term = ontology.getTermMap().get(tid);
                String row = String.format("""
                                <tr>
                                        <td><a href="%s">%s</a></td>
                                        <td>%s</td>
                                        <td>%s</td>
                                      </tr>
                                """,
                        term.id().getValue(),
                        term.id().getValue(),
                        term.getName(),
                        term.getDefinition() != null ? term.getDefinition() : "");
                sb.append(row);
            }
            sb.append("\n");
        }
        return sb.toString();
    }




}
