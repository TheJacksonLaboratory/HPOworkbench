package org.monarchinitiative.hpoworkbench.excel;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.Dbxref;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermSynonym;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getParentTerms;

/**
 * A class for exporting all or part of the HPO as an excel file.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class Hpo2ExcelExporter {
    private static final Logger logger = LogManager.getLogger();

    private final HpoOntology ontology;

    private final static String EMPTY_STRING="";

    public Hpo2ExcelExporter(HpoOntology onto) {
        this.ontology=onto;
        if (ontology==null) {
            logger.error("ontology is null in COTR");
        }
    }




    public void exportToExcelFile(String path) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("HPO Export");


        int rowNum = 0;
        logger.trace("Creating excel");
        Row header = sheet.createRow(rowNum++);
        int colNum=0;
        for (String h : getHeader()) {
            Cell cell = header.createCell(colNum++);
            cell.setCellValue(h);
        }
        // now do the ontology
        Set<TermId> tids = ontology.getNonObsoleteTermIds();
        List<TermId> lst = new ArrayList<>(tids);
        Collections.sort(lst);
        for (TermId t : lst) {
            String[] items=  getRow(t);
            Row row = sheet.createRow(rowNum++);
            for (int i=0;i<items.length;++i) {
                Cell cell = row.createCell(i);
                cell.setCellValue(items[i]);
            }
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(path);
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //ouputHPO();
    }

    private String getParents(TermId childId){
        if (childId==null) {
            logger.error("attempt to getParents with null childId");
            return " ";
        }  else if (ontology==null) {
            logger.error("Attempt to getParents with null ontology object");
            return " ";
        } else if (ontology.isRootTerm(childId)) {
            return " ";
        }
        Set<TermId> parents = getParentTerms(ontology,childId);
        if (parents.isEmpty()) return " ";
        else return parents.stream().map(TermId::getIdWithPrefix).collect(Collectors.joining("; "));
    }


    private String getXrefs(Term term) {
        List<Dbxref> dbxlst =  term.getXrefs();
        if (dbxlst==null || dbxlst.isEmpty()) return EMPTY_STRING;
        return dbxlst.stream().map(Dbxref::getName).collect(Collectors.joining("; "));
    }


    private String[] getHeader() {
        String header[]={"Label","id","definition","comment","synonyms","xrefs","parents"};
        return header;
    }


    private String[] getRow(TermId tid) {
        Term term = ontology.getTermMap().get(tid);
        String row[] = new String[7];
        if (term == null) {
            logger.error("Could not get term object for tid=%s"+tid.getIdWithPrefix());
            return row;
        }
        row[0]=term.getName(); // 1. label
        row[1]=tid.getIdWithPrefix(); // 2. term id
        row[2]=term.getDefinition()!=null?term.getDefinition():"[no definition]";
        row[3]=term.getComment()!=null?term.getComment():"-";
        row[4]=term.getSynonyms().stream().map(TermSynonym::getValue).collect(Collectors.joining("; "));
        row[5]=getXrefs(term);
        row[6]=getParents(tid);
        return row;
    }

}
