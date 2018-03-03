package org.monarchinitiative.hpoworkbench.excel;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.formats.hpo.HpoTerm;
import org.monarchinitiative.phenol.graph.data.Edge;
import org.monarchinitiative.phenol.ontology.data.Dbxref;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermSynonym;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class Hpo2ExcelExporter {
    private static final Logger logger = LogManager.getLogger();

    private final HpoOntology ontology;

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
        Set<TermId> tids = ontology.getAllTermIds();
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //ouputHPO();
    }

    private String getParents(TermId childId){
        Set<String> parents = new HashSet<>();
        if (childId==null) {
            logger.error("attempt to getParents with null childId");
            return " ";
        }
        if (ontology==null) {
            logger.error("Attempt to getParents with null ontology object");
            return " ";
        }
        if (ontology.getGraph()==null) {
            logger.error("Graph object is null (should never happen)");
            return " ";
        }
        if (ontology.isRootTerm(childId)) return " ";
        Iterator it = null;
        try {
            ontology.getGraph().outEdgeIterator(childId);
        } catch (Exception e) {
            logger.error(String.format("Null ptr for %s [%s]",ontology.getTermMap().get(childId).getName(),childId.getIdWithPrefix()));
            return " ";
        }
        if (it==null) {
            logger.error(String.format("Attempt to use null iterator in getParents for %s [%s]",ontology.getTermMap().get(childId).getName(),childId.getIdWithPrefix()));
            return " ";

        }
        if (it==null) return " ";
        while (it.hasNext()) {
            Edge<TermId> edge = (Edge<TermId>)it.next();
            TermId par = edge.getDest();
            parents.add(ontology.getTermMap().get(par).getName());
        }
        if (parents.isEmpty()) return " ";
        else return parents.stream().collect(Collectors.joining("; "));
    }


    private String getXrefs(HpoTerm term) {
        List<Dbxref> dbxlst =  term.getXrefs();
        return dbxlst.stream().map(Dbxref::getName).collect(Collectors.joining("; "));
    }


    private String[] getHeader() {
        String header[]={"Label","id","definition","comment","synonyms","xrefs","parents"};

        return header;
    }


    private String[] getRow(TermId tid) {
        HpoTerm term = ontology.getTermMap().get(tid);
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


    public void ouputHPO() {
        Set<TermId> tids = ontology.getAllTermIds();
        List<TermId> lst = new ArrayList<>(tids);
        Collections.sort(lst);
        for (TermId t : lst) {
            System.out.println(t.getIdWithPrefix());
        }
    }

}
