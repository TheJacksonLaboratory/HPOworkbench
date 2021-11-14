package org.monarchinitiative.hpoworkbench.html;

import org.monarchinitiative.hpoworkbench.annotation.CategoryMerge;
import org.monarchinitiative.hpoworkbench.annotation.SubClassTermPair;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.annotations.formats.hpo.category.HpoCategory;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class OmimOrphanetDiseaseHtmlGenerator {
    private static final Logger logger = LoggerFactory.getLogger(OmimOrphanetDiseaseHtmlGenerator.class);

    private final static String EMPTY_STRING="";


    public static String getHTML(HpoDisease omim, HpoDisease orpha, Map<HpoCategory,CategoryMerge> catmap, Ontology ontology) {
        return getMergerTable(catmap,ontology);
    }



    private static String getTableFramework(String title, String disease1name, String disease2name) {
        return String.format("""
                <table class="zebra">
                    <caption  style="color:#222;text-shadow:0px 1px 2px #555;font-size:24px;">%s</caption>
                    <thead>
                      <tr class="myheader">
                        <th>Id</th><th>definition</th>
                      </tr>
                    </thead>
                    <tfoot>
                      <tr>
                        <td colspan="3">More information: <a href="http://www.human-phenotype-ontology.org">HPO Website</a></td>
                      </tr>
                    </tfoot>""", title);
    }

    /**
     * Returns rows for annotations that are the same in both Omim and Orphanet
     * @param termIdList
     * @param ontology
     * @return
     */
    private static String gedtIdenticalInBothDatabasesRows(List<TermId> termIdList, Ontology ontology ) {
        if (termIdList==null || termIdList.isEmpty()) return EMPTY_STRING;
        StringBuilder sb = new StringBuilder();
        String row1="<tr class=\"shared\"><td colspan=\"2\"><i><b>OMIM and Orphanet: identical</b></i></td></tr>";
        sb.append(row1);
        for (TermId tid : termIdList) {
            Term term = ontology.getTermMap().get(tid);
            String row = String.format("""
                            <tr class="shared">
                                    <td><a href="%s">%s [%s]</a></td>
                                    <td>%s</td>
                                  </tr>
                            """,
                    term.getId().getValue(),
                    term.getName(),
                    term.getId().getValue(),
                    term.getDefinition());

            sb.append(row);
        }
        return sb.toString();
    }

    /**
     * Create table rows with one annotation being a subclass of the other.
     * @param catmerge
     * @param ontology
     * @return
     */
    private static String getSubclassRows(CategoryMerge catmerge, Ontology ontology) {
        String db1=catmerge.getDb1();
        String db2=catmerge.getDb2();
        StringBuilder sb = new StringBuilder();
        List<SubClassTermPair> sclasspairs = catmerge.getD1subclassOfd2();
        if (sclasspairs==null || sclasspairs.isEmpty()) {
            sb.append(EMPTY_STRING);
        } else {
            String row1="<tr class=\"subclazz\"><td colspan=\"2\"><i><b>OMIM and Orphanet: annotations in subclass relation</b></i></td></tr>";
            sb.append(row1);
            for (SubClassTermPair tscp : sclasspairs) {
                TermId t1 = tscp.getSubTid();
                TermId t2 = tscp.getSuperTid();
                String label1 = ontology.getTermMap().get(t1).getName();
                String label2 = ontology.getTermMap().get(t2).getName();
                sb.append(String.format("<tr class=\"subclazz\"><td>%s [%s] (%s) <br/>subclass of<br/> %s [%s] (%s)</td>" +
                                "<td>%s: %s<br/>%s: %s</td> </tr>\n",
                        label1, t1.getValue(), db1,
                        label2, t2.getValue(), db2,
                        t1.getValue(),ontology.getTermMap().get(t1).getDefinition(),
                        t2.getValue(),ontology.getTermMap().get(t2).getDefinition()
                        ));
            }
        }
        sclasspairs = catmerge.getD2subclassOfd1();
        if (sclasspairs==null || sclasspairs.isEmpty()) {
            sb.append(EMPTY_STRING);
        } else {
            for (SubClassTermPair tscp : sclasspairs) {
                TermId t1 = tscp.getSubTid();
                TermId t2 = tscp.getSuperTid();
                String label1 = ontology.getTermMap().get(t1).getName();
                String label2 = ontology.getTermMap().get(t2).getName();
                sb.append(String.format("<tr class=\"subclazz\"><td>%s [%s] (%s) <br/>subclass of<br/> %s [%s] (%s)</td>" +
                                "<td>%s: %s<br/>%s: %s</td> </tr>\n",
                        label1, t1.getValue(), db1,
                        label2, t2.getValue(), db2,
                        t1.getValue(),ontology.getTermMap().get(t1).getDefinition(),
                        t2.getValue(),ontology.getTermMap().get(t2).getDefinition()
                ));
            }
        }
        return sb.toString();
    }

    private static String getOnlyOneDiseaseRows(CategoryMerge catmerge, Ontology ontology){
        String db1=catmerge.getDb1();
        String db2=catmerge.getDb2();
        StringBuilder sb = new StringBuilder();
        if (! catmerge.hasTermsUniqueToOnlyOneDisease()) {
            return EMPTY_STRING;
        }
        String row1="<tr class=\"unrelated\"><td colspan=\"2\"><i><b>Annotation unique to OMIM or Orphanet</b></i></td></tr>";
        sb.append(row1);
        for (TermId t1 : catmerge.getDisease1onlyTerms()) {
            String label = ontology.getTermMap().get(t1).getName();
            String definition=ontology.getTermMap().get(t1).getDefinition();
            sb.append(String.format("<tr class=\"unrelated\"><td>%s [%s]</td><td>%s only<br/>%s</td></tr>",label,t1.getValue(),db1,definition));
        }
        for (TermId t2 : catmerge.getDisease2onlyTerms()) {
            String label = ontology.getTermMap().get(t2).getName();
            String definition=ontology.getTermMap().get(t2).getDefinition();
            sb.append(String.format("<tr class=\"unrelated\"><td>%s [%s]</td><td>%s only</td></tr>",label,t2.getValue(),db2));
        }
        return sb.toString();
    }


    /**
     * Create a table with the annotations for Omim and Orphanet side by side.
     * @param catmap
     * @param ontology
     * @return
     */
    private static String getMergerTable(Map<HpoCategory,CategoryMerge> catmap, Ontology ontology) {
        StringBuilder sb = new StringBuilder();
        for (HpoCategory cat : catmap.keySet()) {
            CategoryMerge catmerge = catmap.get(cat);
            logger.trace("Get table for cat {} ({})",cat.getLabel(),catmerge.getCounts());
            String disease1name = catmerge.getDisease1name();
            String disease2name = catmerge.getDisease2name();
            String title = cat.getLabel();
            sb.append(getTableFramework(title, disease1name,disease2name));
            List<TermId> termIdList = catmerge.getCommonTerms();
            sb.append(gedtIdenticalInBothDatabasesRows(termIdList,ontology));
            sb.append(getSubclassRows(catmerge,ontology));
            sb.append(getOnlyOneDiseaseRows(catmerge,ontology));
            sb.append("\n");
        }
        return sb.toString();
    }

}
