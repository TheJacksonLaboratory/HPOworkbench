package org.monarchinitiative.hpoworkbench.word;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.monarchinitiative.hpoworkbench.github.GitHubIssue;
import org.xml.sax.SAXParseException;

import javax.xml.bind.JAXBException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class GitIssue2Doc4J {
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<GitHubIssue> issues;
    private final String githubLabel;


    public GitIssue2Doc4J(List<GitHubIssue> il, String label){
        issues=il;
        githubLabel=label;
    }


    public void outputFile(String fname) {
        try {
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
            MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
            introductoryParagraph(mdp);
        for (GitHubIssue gi : issues) {
            writeGitIssueAsParagraph(gi,mdp);
        }
            writeWordFile(wordMLPackage,fname);
        } catch (InvalidFormatException ife) {
            ife.printStackTrace();
        } catch (JAXBException jbe) {
            jbe.printStackTrace();
        } catch (SAXParseException se) {
            se.printStackTrace();
        }
    }


    private void introductoryParagraph(MainDocumentPart document) throws JAXBException,SAXParseException {
        document.addStyledParagraphOfText("Title", String.format("Human Phenotype Ontology Issues for %s",githubLabel));
           document.addParagraphOfText("This document is for suggesting revisions to the HPO.");
        document.addParagraphOfText(" Please add your comments and adivce directly to this document."+
                " Please use Word's track changes feature to show your work.");
    }


    private void writeGitIssueAsParagraph(GitHubIssue gitissue,MainDocumentPart document) throws JAXBException  {
        String myTitle;
        if (gitissue.hasValidIssueNumber()) {
            myTitle=String.format("%s) %s",gitissue.getIssueNumber(),gitissue.getTitle());
        } else {
            myTitle=gitissue.getTitle();
        }
        document.addStyledParagraphOfText("Subtitle", myTitle);
        String body = gitissue.getBody();
        body=body.replaceAll("\\n+","@@@").replaceAll("\\s+"," ").replaceAll("@@@","\n");
        document.addParagraphOfText(body);
        List<String> comments=gitissue.getComments();
        for (String c: comments) {
            document.addParagraphOfText("\u2022 " + c);
        }
    }


    private void writeWordFile(WordprocessingMLPackage document, String outPath){
        try {
            FileOutputStream out = new FileOutputStream(outPath);
            document.save(new java.io.File(outPath));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Docx4JException e) {
            e.printStackTrace();
        }
        LOGGER.trace("Saved Word file to "+ outPath);
    }

}
