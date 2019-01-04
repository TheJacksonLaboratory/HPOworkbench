package org.monarchinitiative.hpoworkbench.word;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.*;
import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.phenol.formats.hpo.HpoOntology;
import org.monarchinitiative.phenol.ontology.data.*;


import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getChildTerms;

/**
 * This class coordinates the production and output of an RTF file that contains a table with all of the terms
 * that emanate from a certain term in the HPO, for instance, abnormal immune physiology. It is intended to produce
 * an RTF file that can be easily distributed as a Word file to collaborators who will enter corrections and additions
 * to a set of HPO terms in a specific area of medicine.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.2
 */
public class Hpo2Word {
    private static final Logger LOGGER = LogManager.getLogger();
    /** Number of unique terms we have output in this file. */
    private int n_terms_output=0;
    /** HPO Ontology object. */
    private HpoOntology hpoOntology=null;
    private static String DEFAULT_START_TERM="HP:0000118";



    private String startTerm="HP:0002715"; // immunology




    public Hpo2Word(String filename, String term) throws  IOException {
        startTerm=term;
        LOGGER.info("We will input HPO file at ?");
        inputHPOdata(null);
        LOGGER.info("Starting to create WORD file");
        XWPFDocument document = new XWPFDocument();
        introductoryParagraph(document);
        String filename2="hpo.doc";
        LOGGER.info("We will create table");
        writeTableFromTerm(document,startTerm);
        writeWordFile(document,filename2);

    }



    private void introductoryParagraph(XWPFDocument document) {
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
       // The content of a paragraph needs to be wrapped in an XWPFRun object.
        XWPFRun titleRun = title.createRun();
        titleRun.setText("Human Phenotype Ontology: ");
        titleRun.setColor("009933");
        titleRun.setBold(true);
        titleRun.setFontFamily("Courier");
        titleRun.setFontSize(20);

        XWPFParagraph subTitle = document.createParagraph();
        subTitle.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun subTitleRun = subTitle.createRun();
        subTitleRun.setText("This document is for suggesting revisions to the HPO");
        subTitleRun.setColor("00CC44");
        subTitleRun.setFontFamily("Courier");
        subTitleRun.setFontSize(16);
        subTitleRun.setTextPosition(20);
        subTitleRun.setUnderline(UnderlinePatterns.DOT_DOT_DASH);

        XWPFParagraph para1 = document.createParagraph();
        para1.setAlignment(ParagraphAlignment.BOTH);
        String string1 = "The hierarchical structure of the HPO has been converted into a Table that tries to suggest" +
                " the hierarchy. Note that a term is shown only once in the Table (even if it has multiple parents), " +
                " therefore, please use the HPO Workbench or another browser to check the full hierarchical structure" +
                " of the HPO.";
        XWPFRun para1Run = para1.createRun();
        para1Run.setText(string1);
        XWPFParagraph para2 = document.createParagraph();
        para2.setAlignment(ParagraphAlignment.BOTH);
        String string2 = "Please use Word's track changes feature to show your work. Add comments or write directly " +
                "into the current text.";
        XWPFRun para2Run = para1.createRun();
        para2Run.setText(string2);
    }


    private void writeWordFile(XWPFDocument document,String outPath) throws IOException {
        FileOutputStream out = new FileOutputStream(outPath);
        document.write(out);
        out.close();
        document.close();
    }




    private static void setRun (XWPFRun run, String fontFamily, int fontSize, String colorRGB , String text , boolean bold , boolean addBreak) {
        run.setFontFamily(fontFamily);
        run.setFontSize(fontSize);
        run.setColor(colorRGB);
        run.setText(text);
        run.setBold(bold);
        if (addBreak) run.addBreak();
    }


    private void writeTableFromTerm(XWPFDocument document,String id) {
        startTerm=id;
        XWPFTable table    = document.createTable();

        //create first row
        XWPFTableRow tableRowOne = table.getRow(0);
        tableRowOne.getCell(0).setText("Level");
        tableRowOne.addNewTableCell().setText("Term");
        tableRowOne.addNewTableCell().setText("Definition");
        tableRowOne.addNewTableCell().setText("Comment");
        tableRowOne.addNewTableCell().setText("Synonyms");
        //create second row
        Map<TermId,Term> termmap = hpoOntology.getTermMap();
        Set<TermId> previouslyseen=new HashSet<>();
        Stack<Pair<TermId,Integer>> stack = new Stack<>();
        if (id==null) {
            LOGGER.error("Attempt to create pretty format HPO Term with null id");
            return;
        }
        TermId tid = TermId.of(id);
        stack.push(new Pair<>(tid,1));

        int c=0;

        while (! stack.empty() ) {
            Pair<TermId,Integer> pair = stack.pop();
            TermId termId=pair.first;
            Integer level=pair.second;
            Term hterm = termmap.get(termId);
            if (! previouslyseen.contains(termId)) {
                // we have not yet output this term!
                XWPFTableRow tableRow = table.createRow();
                tableRow.getCell(0).setText(String.valueOf(level));
                tableRow.addNewTableCell().setText(hterm.getName());
                tableRow.addNewTableCell().setText(hterm.getDefinition());
                tableRow.addNewTableCell().setText(hterm.getComment());
                tableRow.addNewTableCell().setText(hterm.getSynonyms().
                        stream().
                        map(TermSynonym::getValue).
                        collect(Collectors.joining(";")));
                previouslyseen.add(termId);
                Set<TermId> children = getChildTerms(hpoOntology,tid,false);
                for (TermId t:children) {
                    stack.push(new Pair<>(t,level+1));
                }
            } else {
                XWPFTableRow tableRow = table.createRow();
                tableRow.getCell(0).setText(String.valueOf(level));
                tableRow.addNewTableCell().setText(hterm.getName());
                tableRow.addNewTableCell().setText("Term previously shown (dependent on another parent)");
                tableRow.addNewTableCell().setText("");
                tableRow.addNewTableCell().setText("");
            }

        }

        //run.setText("End of table");
        int[] cols = {2000, 2000, 2000, 2000, 2000};

        for (int i = 0; i < table.getNumberOfRows(); i++) {
            XWPFTableRow row = table.getRow(i);
            int numCells = row.getTableCells().size();
            for (int j = 0; j < numCells; j++) {
                XWPFTableCell cell = row.getCell(j);
                cell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(2000));
            }
        }

    }

    /**
     * Create a set of rows that will be displayed as an RTF table. Noting that the HPO has multiple parentage,
     * only show any one subhierarchy once.
     * @return Table object
     */
    private RtfTable createTable() {
        Map<TermId,Term> termmap = hpoOntology.getTermMap();
        Set<TermId> previouslyseen=new HashSet<>();
        String id = startTerm;
        Stack<Pair<TermId,Integer>> stack = new Stack<>();
        if (id==null) {
            LOGGER.error("Attempt to create pretty format HPO Term with null id");
            return null;
        }
        TermId tid =  TermId.of(id);
        stack.push(new Pair<>(tid,1));
        ArrayList<HpoRtfTableRow> rtfrows = new ArrayList<>();

        while (! stack.empty() ) {
            Pair<TermId,Integer> pair = stack.pop();
            TermId termId=pair.first;
            Integer level=pair.second;
            Term hterm = termmap.get(termId);
            if (previouslyseen.contains(termId)) {
                // we have already output this term!
                HpoRtfTableRow hrow = new HpoRtfTableRow(level, hterm,
                        "\\b Term previously shown (dependent on another parent)\\b0");
                rtfrows.add(hrow);
                continue;
            } else {
                previouslyseen.add(termId);
            }
            Set<TermId> children = getChildTerms(hpoOntology,tid,false);
            for (TermId t:children) {
                stack.push(new Pair<>(t,level+1));
            }
            HpoRtfTableRow hrow = new HpoRtfTableRow(level, hterm);
            rtfrows.add(hrow);
        }

        return new RtfTable(rtfrows);
    }


    /** Input the HPO ontology file into {@link #hpoOntology}. */
    private void inputHPOdata(String hpo) {
        if (hpo==null)hpo="data/hp.obo";
        LOGGER.trace(String.format("inputting HPO ontology from file %s.",hpo));
        HPOParser parser = new HPOParser(hpo);
        hpoOntology=parser.getHPO();

    }



}
