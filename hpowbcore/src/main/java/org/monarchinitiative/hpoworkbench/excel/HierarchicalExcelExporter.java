package org.monarchinitiative.hpoworkbench.excel;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.monarchinitiative.hpoworkbench.exception.HPOException;
import org.monarchinitiative.hpoworkbench.word.Pair;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermPrefix;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static org.monarchinitiative.hpoworkbench.excel.TermRow.getHeader;
import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getChildTerms;

/**
 * The purpose of this class is to export a portion of the HPO file as an excel sheet suggests the hierarchy of the
 * HPO by using a different column for each level.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.2.13
 */
public class HierarchicalExcelExporter {
    private static final Logger logger = LogManager.getLogger();
    private final HpoOntology ontology;
    private static final TermPrefix HPPREFIX = new TermPrefix("HP");
    /** The term of the subhierarchy of the HPO that we will export. */
    private final Term subhierarchyRoot;
    /** Ordered list of terms and their attributes for all terms that descend from {@link #subhierarchyRoot}.
     * The {@link TermRow} objects have all of the data we need to export one row in Excel.
     */
    private final ArrayList<TermRow> termRowList=new ArrayList<>();
    /** The maximum depth of the hierarchy that we will export. */
    private int maxlevel=0;

    /**
     * @param onto Reference to the HPO ontology
     * @param selectedTerm term that defines the subhierarchy to export.
     */
    public HierarchicalExcelExporter(HpoOntology onto, Term selectedTerm) {
        this.ontology=onto;
        if (ontology==null) {
            logger.error("ontology is null in COTR");
        }
        subhierarchyRoot=selectedTerm;
    }



    public void exportToExcel(String newfilename) throws HPOException {
        calculateRowHierarchy();
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(String.format("HPO Export (%s)",subhierarchyRoot.getName()));
        int rowNum = 0;
        Row header = sheet.createRow(rowNum++);
        int colNum=0;
        for (String h : getHeader(maxlevel)) {
            Cell cell = header.createCell(colNum++);
           cell.setCellValue(h);
       }
        // now do the ontology
        for (TermRow trow : termRowList) {
            String[] items=  trow.getItems(maxlevel);
            Row row = sheet.createRow(rowNum++);
            for (int i=0;i<items.length;++i) {
                Cell cell = row.createCell(i);
                cell.setCellValue(items[i]);
            }
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(newfilename);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            throw new HPOException(String.format("Could not find file %s [%s]",newfilename,e.getMessage()));
        } catch (IOException e) {
            throw new HPOException(String.format("I/O exception in excel export [%s]",e.getMessage()));
        }
    }


    /**
     * Create a set of rows that will be displayed as an RTF table. Noting that the HPO has multiple parentage,
     * only show any one subhierarchy once. This function fills the list {@link #termRowList}.
     */
    private void calculateRowHierarchy() {
        Set<TermId> previouslyseen=new HashSet<>();
        Stack<Pair<TermId,Integer>> stack = new Stack<>();

        TermId tid = subhierarchyRoot.getId();
        stack.push(new Pair<>(tid,1));

        while (! stack.empty() ) {
            Pair<TermId,Integer> pair = stack.pop();
            TermId termId=pair.first;
            Integer level=pair.second;
            Term hterm = ontology.getTermMap().get(termId);
            if (previouslyseen.contains(termId)) {
                // we have already output this term!
                TermRow hrow = new TermRow(level, hterm,
                        "Term previously shown (dependent on another parent)");
                termRowList.add(hrow);
                continue;
            } else {
                previouslyseen.add(termId);
            }
            Set<TermId> children = getChildTerms(ontology,tid,false);
            for (TermId t:children) {
                stack.push(new Pair<>(t,level+1));
            }
            termRowList.add(new TermRow(level,hterm));
            if (level>maxlevel)maxlevel=level;
        }
    }


}
