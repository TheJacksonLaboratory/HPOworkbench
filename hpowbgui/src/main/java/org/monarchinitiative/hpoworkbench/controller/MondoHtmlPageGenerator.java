package org.monarchinitiative.hpoworkbench.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.annotation.AnnotationMerger;
import org.monarchinitiative.hpoworkbench.annotation.CategoryMerge;
import org.monarchinitiative.hpoworkbench.annotation.HpoCategory;
import org.monarchinitiative.hpoworkbench.html.MondoTermHtmlGenerator;
import org.monarchinitiative.hpoworkbench.html.OmimOrphanetDiseaseHtmlGenerator;
import org.monarchinitiative.hpoworkbench.html.SingleDiseaseHTMLGenerator;
import org.monarchinitiative.phenol.formats.generic.GenericTerm;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;

import java.util.Map;

/**
 * Convenience class to generate HTML code to display data about a MONDO entry.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class MondoHtmlPageGenerator {
    private static final Logger logger = LogManager.getLogger();




    private static final String HTML_TEMPLATE = "<!DOCTYPE html>" +
            "<html lang=\"en\"><head>" +
            "<style>%s</style>\n" +
            "<meta charset=\"UTF-8\"><title>Mondo browser</title></head>" +
            "<body>" +
            "%s %s %s\n" + // the Mondo , summary, and the annotation HTML code go here
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



    static String getHTML(GenericTerm mondoTerm, HpoDisease omim, HpoDisease orpha, HpoOntology ontology) {
        AnnotationMerger merger=new AnnotationMerger(omim,orpha, ontology);
        merger.merge();
        Map<HpoCategory,CategoryMerge> catmap = merger.getMergedCategoryMap();

        String mondoHtml=MondoTermHtmlGenerator.getHTML(mondoTerm);

        String table;
        String summary;
        if (omim==null && orpha==null) {
            summary="<br/>";
            return String.format(HTML_TEMPLATE,CSS,mondoHtml,summary,"<p>No OMIM/Orphanet disease model found</p>");
        } else if (orpha==null) {
            summary=String.format("<h2>Merge results</h2><p>Found only OMIM annotations: %s (%s)",omim.getName(),omim.getDiseaseDatabaseId());
            table=SingleDiseaseHTMLGenerator.getHTML(omim,ontology);
        } else if (omim==null) {
            summary=String.format("<h2>Merge results</h2><p>Found only Orphanet annotations: %s (%s)",orpha.getName(),orpha.getDiseaseDatabaseId());
            table=SingleDiseaseHTMLGenerator.getHTML(orpha,ontology);
        } else {
            logger.trace("About to call getMergerTable, catmap size is "+catmap.size());
            summary=String.format("<h2>Merge results</h2><p>Found OMIM and Orphanet annotations.<br/>" +
                    "OMIM: %s (%s) <br/> Orphanet: %s (%s)</p>",
                    omim.getName(),omim.getDiseaseDatabaseId(),
                    orpha.getName(),orpha.getDiseaseDatabaseId());
            table=OmimOrphanetDiseaseHtmlGenerator.getHTML(omim,orpha,catmap,ontology);
        }
        return String.format(HTML_TEMPLATE, CSS,  mondoHtml, summary, table);
    }



}
