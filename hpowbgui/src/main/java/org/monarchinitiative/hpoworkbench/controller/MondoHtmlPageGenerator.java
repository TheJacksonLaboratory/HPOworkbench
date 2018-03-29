package org.monarchinitiative.hpoworkbench.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.annotation.AnnotationMerger;
import org.monarchinitiative.hpoworkbench.annotation.CategoryMerge;
import org.monarchinitiative.hpoworkbench.annotation.HpoCategory;
import org.monarchinitiative.hpoworkbench.annotation.SubClassTermPair;
import org.monarchinitiative.phenol.formats.generic.GenericTerm;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.HpoTerm;
import org.monarchinitiative.phenol.ontology.data.Dbxref;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermSynonym;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Convenience class to generate HTML code to display data about a MONDO entry.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class MondoHtmlPageGenerator {
    private static final Logger logger = LogManager.getLogger();


    private final static String EMPTY_STRING="";

    private static final String HTML_TEMPLATE = "<!DOCTYPE html>" +
            "<html lang=\"en\"><head>" +
            "<style>%s</style>\n" +
            "<meta charset=\"UTF-8\"><title>Mondo browser</title></head>" +
            "<body>" +
            "<h1>%s</h1>" +
            "<p><b>ID:</b> %s</p>" +
            "<p><b>Definition:</b> %s</p>" +
            "<p><b>Dbxref:</b> %s</p>" +
            "<p><b>Synonyms:</b> %s</p>" +
            "%s\n" +
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



    static String getHTML(GenericTerm term, HpoDisease omim, HpoDisease orpha, HpoOntology ontology) {
        if (omim==null) {
            logger.warn("Attempt to getHTML for null OMIM disease");
        }
        if (orpha==null) {
            logger.warn("Attempt to getHTML for null Orphanet disease");
        }
        AnnotationMerger merger=new AnnotationMerger(omim,orpha, ontology);
        merger.merge();
        Map<HpoCategory,CategoryMerge> catmap = merger.getMergedCategoryMap();

        String termID = term.getId().getIdWithPrefix();
        String synonyms = (term.getSynonyms() == null) ? EMPTY_STRING : term.getSynonyms().stream().map(TermSynonym::getValue)
                .collect(Collectors.joining("; "));
        String definition = (term.getDefinition() == null) ? EMPTY_STRING : term.getDefinition();
        List<Dbxref>  dbxlist = term.getXrefs();
        String comment = (dbxlist == null||dbxlist.isEmpty()) ?
                EMPTY_STRING :
                dbxlist.stream().map(Dbxref::getName).collect(Collectors.joining("; "));
        logger.trace("About to call getMergerTable, catmap size is "+catmap.size());
        String tabula = getMergerTable(catmap,ontology);
        return String.format(HTML_TEMPLATE, CSS, term.getName(), termID, definition, comment, synonyms, tabula);
    }


    private static String getTableFramework(String title, String disease1name, String disease2name) {
        return String.format("  <table class=\"zebra\">\n" +
                "    <caption  style=\"color:#222;text-shadow:0px 1px 2px #555;font-size:24px;\">%s</caption>\n" +
                "    <thead>\n" +
                "      <tr>\n" +
                "        <th>Id</th><th>%s</th><th>%s</th>\n" +
                "      </tr>\n" +
                "    </thead>\n" +
                "    <tfoot>\n" +
                "      <tr>\n" +
                "        <td colspan=\"3\">More information: <a href=\"http://www.human-phenotype-ontology.org\">HPO Website</a></td>\n" +
                "      </tr>\n" +
                "    </tfoot>", title, disease1name,disease2name);
    }

    private static String getBothDieasesRows(List<TermId> termIdList, HpoOntology ontology ) {
        if (termIdList==null || termIdList.isEmpty()) return EMPTY_STRING;
        StringBuilder sb = new StringBuilder();
        for (TermId tid : termIdList) {
            HpoTerm term = ontology.getTermMap().get(tid);
            String row = String.format("<tr>\n" +
                            "        <td rowspan=\"2\">Terms shared by both diseases</td>\n" +
                            "        <td rowspan=\"2\"><a href=\"%s\">%s [%s]</a></td>\n" +
                            "      </tr>\n",
                    term.getId().getIdWithPrefix(),
                    term.getName(),
                    term.getId().getIdWithPrefix());
            sb.append(row);
        }
        return sb.toString();
    }

    private static String getSubclassRows(CategoryMerge catmerge, HpoOntology ontology) {
        String diseaseName1=catmerge.getDisease1name();
        String diseaseName2=catmerge.getDisease2name();
        StringBuilder sb = new StringBuilder();

        List<SubClassTermPair> sclasspairs = catmerge.getD1subclassOfd2();
        if (sclasspairs==null || sclasspairs.isEmpty()) {
            sb.append(EMPTY_STRING);
        } else {
            for (SubClassTermPair tscp : sclasspairs) {
                TermId t1 = tscp.getSubTid();
                TermId t2 = tscp.getSuperTid();
                String label1 = ontology.getTermMap().get(t1).getName();
                String label2 = ontology.getTermMap().get(t2).getName();
                sb.append(String.format("         <td rowspan=\"2\">s [%s] (%s)is subclass of %s [%s]</td>\n",
                        label1, t1.getIdWithPrefix(), diseaseName1,
                        label2, t2.getIdWithPrefix(), diseaseName2));
            }
        }
        sclasspairs = catmerge.getD2subclassOfd1();
        if (sclasspairs==null || sclasspairs.isEmpty()) {
            sb.append(EMPTY_STRING);
        } else {
            for (SubClassTermPair tscp : sclasspairs) {
                TermId t2 = tscp.getSubTid();
                TermId t1 = tscp.getSuperTid();
                String label1 = ontology.getTermMap().get(t1).getName();
                String label2 = ontology.getTermMap().get(t2).getName();
                sb.append(String.format("<td rowspan=\"2\">%s [%s] (%s) is subclass of %s [%s] (%s)</td>",
                        label2, t2.getIdWithPrefix(), diseaseName2,
                        label1, t1.getIdWithPrefix(), diseaseName1));
            }
        }
        return sb.toString();
    }

    private static String getOnlyOneDiseaseRows(CategoryMerge catmerge, HpoOntology ontology){
        String diseaseName1=catmerge.getDisease1name();
        String diseaseName2=catmerge.getDisease2name();
        StringBuilder sb = new StringBuilder();
        for (TermId t1 : catmerge.getDisease1onlyTerms()) {
            String label = ontology.getTermMap().get(t1).getName();
            sb.append(String.format("<td rowspan=\"2\">%s only: %s [%s]</td>",diseaseName1,label,t1.getIdWithPrefix()));
        }
        for (TermId t2 : catmerge.getDisease2onlyTerms()) {
            String label = ontology.getTermMap().get(t2).getName();
            sb.append(String.format("<td rowspan=\"2\">%s only: %s [%s]</td>",diseaseName2,label,t2.getIdWithPrefix()));
        }
        return sb.toString();
    }




    private static String getMergerTable(Map<HpoCategory,CategoryMerge> catmap, HpoOntology ontology) {
        StringBuilder sb = new StringBuilder();
        for (HpoCategory cat : catmap.keySet()) {
            CategoryMerge catmerge = catmap.get(cat);
            logger.trace("Get tanle for cat "+cat.getLabel());
            String disease1name = catmerge.getDisease1name();
            String disease2name = catmerge.getDisease2name();
            String title = cat.getLabel();//String.format(template, cat.getLabel(), cat.getNumberOfAnnotations());
            sb.append(getTableFramework(title, disease1name,disease2name));
            List<TermId> termIdList = catmerge.getDisease1onlyTerms();
            sb.append(getBothDieasesRows(termIdList,ontology));
            sb.append(getSubclassRows(catmerge,ontology));
            sb.append(getOnlyOneDiseaseRows(catmerge,ontology));
            sb.append("\n");
        }
        return sb.toString();
    }
}
