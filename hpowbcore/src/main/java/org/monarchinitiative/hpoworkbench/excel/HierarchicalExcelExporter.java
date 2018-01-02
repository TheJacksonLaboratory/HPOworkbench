package org.monarchinitiative.hpoworkbench.excel;

import com.github.phenomics.ontolib.formats.hpo.HpoOntology;
import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import com.github.phenomics.ontolib.graph.data.Edge;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermId;
import com.github.phenomics.ontolib.ontology.data.ImmutableTermPrefix;
import com.github.phenomics.ontolib.ontology.data.TermId;
import com.github.phenomics.ontolib.ontology.data.TermPrefix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.monarchinitiative.hpoworkbench.rtf.Pair;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static org.monarchinitiative.hpoworkbench.excel.TermRow.getHeader;

public class HierarchicalExcelExporter {

    private static final Logger logger = LogManager.getLogger();

    private final HpoOntology ontology;
    private static final TermPrefix HPPREFIX = new ImmutableTermPrefix("HP");
    private String DEFAULT_OUTPUTNAME="hpotest.xlsx";

    private final HpoTerm subhierarchyRoot;

    ArrayList<TermRow> termRowList=new ArrayList<>();

    private int maxlevel=0;


    public HierarchicalExcelExporter(HpoOntology onto, HpoTerm selectedTerm) {
        this.ontology=onto;
        if (ontology==null) {
            logger.error("ontology is null in COTR");
        }
        subhierarchyRoot=selectedTerm;
    }



    public void exportToExcel(String newfilename) {
        calculateRowHierarchy();
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(String.format("HPO Export (%s)",subhierarchyRoot.getName()));
        int rowNum = 0;
        logger.trace("Creating excel");
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    /**
     * Get all the direct children terms of a term
     * @param tid HPO term for which we want to get the children
     * @return set of children term ids of tid.
     */
    private Set<TermId> getChildren(TermId tid) {
        Set<TermId> st = new HashSet<>() ;
        Iterator it = ontology.getGraph().inEdgeIterator(tid);
        while (it.hasNext()) {
            Edge<TermId> egde = (Edge<TermId>) it.next();
            TermId source = egde.getSource();
            st.add(source);
        }
        return st;
    }

    /**
     * Create a set of rows that will be displayed as an RTF table. Noting that the HPO has multiple parentage,
     * only show any one subhierarchy once.
     * @return
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
            HpoTerm hterm = ontology.getTermMap().get(termId);
            if (previouslyseen.contains(termId)) {
                // we have already output this term!
                TermRow hrow = new TermRow(level, hterm,
                        "Term previously shown (dependent on another parent)");
                termRowList.add(hrow);
                continue;
            } else {
                previouslyseen.add(termId);
            }
            Set<TermId> children = getChildren(termId);
            for (TermId t:children) {
                stack.push(new Pair<>(t,level+1));
            }
            termRowList.add(new TermRow(level,hterm));
            if (level>maxlevel)maxlevel=level;
        }
    }



}
