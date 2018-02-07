package org.monarchinitiative.hpoworkbench.word;

import org.apache.poi.xssf.usermodel.XSSFTextParagraph;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLvl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STNumberFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.math.BigInteger;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.monarchinitiative.hpoworkbench.github.GitHubIssue;

public class GitIssue2Word {

    private final List<GitHubIssue> issues;
    private final String githubLabel;


    public GitIssue2Word(List<GitHubIssue> il, String label){
        issues=il;
        githubLabel=label;
    }

    public void outputFile(String fname) {
        XWPFDocument document = new XWPFDocument();
        introductoryParagraph(document);
        for (GitHubIssue gi : issues) {
            writeGitIssueAsParagraph(gi,document);
        }
        writeWordFile(document,fname);
    }


    private void introductoryParagraph(XWPFDocument document) {
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        // The content of a paragraph needs to be wrapped in an XWPFRun object.
        XWPFRun titleRun = title.createRun();
        titleRun.setText("Human Phenotype Ontology Issues for "+githubLabel);
        titleRun.setColor("009933");
        titleRun.setBold(true);
        titleRun.setFontFamily("Courier");
        titleRun.setFontSize(16);

        XWPFParagraph subTitle = document.createParagraph();
        subTitle.setAlignment(ParagraphAlignment.LEFT);

        XWPFRun subTitleRun = subTitle.createRun();
        subTitleRun.setFontSize(14);
        subTitleRun.setText("This document is for suggesting revisions to the HPO");
        subTitleRun.setColor("00CC44");
        subTitleRun.setFontFamily("Courier");
        subTitleRun.setTextPosition(20);
        subTitleRun.setUnderline(UnderlinePatterns.DOT_DOT_DASH);

        XWPFParagraph para1 = document.createParagraph();
        para1.setAlignment(ParagraphAlignment.BOTH);
        subTitleRun.setFontSize(12);
        String string1 = "This document contains a summary of up to 30 open issues for the topic: "+githubLabel+".";
        XWPFRun para1Run = para1.createRun();
        para1Run.setText(string1);
        XWPFParagraph para2 = document.createParagraph();
        para2.setAlignment(ParagraphAlignment.BOTH);
        String string2 = " Please add your comments and adivce directly to this document."+
        " Please use Word's track changes feature to show your work.";
        XWPFRun para2Run = para1.createRun();
        para2Run.setText(string2);
    }


    private void writeGitIssueAsParagraph(GitHubIssue gitissue,XWPFDocument document) {



        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun run = paragraph.createRun();
        run.setFontSize(14);
        run.setColor("000000");
        run.setFontFamily("Courier");
        run.setBold(true);
        String myTitle=null;
        if (gitissue.hasValidIssueNumber()) {
            myTitle=String.format("%s) %s",gitissue.getIssueNumber(),gitissue.getTitle());
        } else {
            myTitle=gitissue.getTitle();
        }
        run.setText(myTitle);

        paragraph = document.createParagraph();
        run = paragraph.createRun();
        run.setFontSize(12);
        run.setColor("000000");
        run.setFontFamily("Courier");
        run.setText(gitissue.getBody());


        paragraph = document.createParagraph();
        run=paragraph.createRun();
        run.setText("Comments:");

        List<String> documentList=gitissue.getComments();


        CTAbstractNum cTAbstractNum = CTAbstractNum.Factory.newInstance();
        //Next we set the AbstractNumId. This requires care.
        //Since we are in a new document we can start numbering from 0.
        //But if we have an existing document, we must determine the next free number first.
        cTAbstractNum.setAbstractNumId(BigInteger.valueOf(0));

/* Bullet list
  CTLvl cTLvl = cTAbstractNum.addNewLvl();
  cTLvl.addNewNumFmt().setVal(STNumberFormat.BULLET);
  cTLvl.addNewLvlText().setVal("•");
*/

///* Decimal list
        CTLvl cTLvl = cTAbstractNum.addNewLvl();
        cTLvl.addNewNumFmt().setVal(STNumberFormat.DECIMAL);
        cTLvl.addNewLvlText().setVal("%1.");
        cTLvl.addNewStart().setVal(BigInteger.valueOf(1));
//*/

        XWPFAbstractNum abstractNum = new XWPFAbstractNum(cTAbstractNum);

        XWPFNumbering numbering = document.createNumbering();

        BigInteger abstractNumID = numbering.addAbstractNum(abstractNum);

        BigInteger numID = numbering.addNum(abstractNumID);
        //XSSFTextParagraph textpara = new XSSFTextParagraph();

        for (String string : documentList) {
            paragraph = document.createParagraph();
            paragraph.setNumID(numID);
            run=paragraph.createRun();
            run.setFontSize(10);
            run.setText(String.format("\\u2022 %s" ,string));
        }

        paragraph = document.createParagraph();


//        String paratext=gitissue.toString();
//        run.setText(paratext);
    }



    private void writeWordFile(XWPFDocument document, String outPath){
        try {
            FileOutputStream out = new FileOutputStream(outPath);
            document.write(out);
            out.close();
            document.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
