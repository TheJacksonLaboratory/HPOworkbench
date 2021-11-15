package org.monarchinitiative.hpoworkbench.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.StringConverter;
import org.monarchinitiative.hpoworkbench.analysis.AnnotationTlc;
import org.monarchinitiative.hpoworkbench.analysis.HpoStats;
import org.monarchinitiative.hpoworkbench.analysis.MondoStats;
import org.monarchinitiative.hpoworkbench.exception.HPOException;
import org.monarchinitiative.hpoworkbench.gui.PopUps;
import org.monarchinitiative.hpoworkbench.html.AnnotationTlcHtmlGenerator;
import org.monarchinitiative.hpoworkbench.html.HpoStatsHtmlGenerator;
import org.monarchinitiative.hpoworkbench.html.MondoStatsHtmlGenerator;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public final class AnalysisTabController {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisTabController.class);

    private final OptionalResources optionalResources;

    /**
     * WebView for displaying details of the analysis.
     */
    @FXML
    private WebView hpoAnalysisWebView;

    @FXML
    private DatePicker datepicker;

    /**
     * WebEngine backing up the {@link #hpoAnalysisWebView}.
     */
    private WebEngine infoWebEngine;

    private final TermId MONDO_ROOT_ID = TermId.of("MONDO:0000001");

    /**
     * Application-specific properties (not the System properties!) defined in the 'application.properties' file that
     * resides in the classpath.
     */
    @Autowired
    private Properties pgProperties;


    @Autowired
    public AnalysisTabController(OptionalResources optionalResources,
                                 @Qualifier("appHomeDir") File hpoWorkbenchDir) {
        this.optionalResources = optionalResources;
         File hpoWorkbenchDir1 = hpoWorkbenchDir;
    }


    @FXML
    public void initialize() {
        logger.info("Initialize");
    }


    @FXML
    private void showHpoStatistics() {
        try {
            HpoStats stats = new HpoStats(optionalResources.getHpoOntology(), optionalResources.getDisease2AnnotationMap());
            String html = HpoStatsHtmlGenerator.getHTML(stats);
            Platform.runLater(() -> {
                infoWebEngine = hpoAnalysisWebView.getEngine();
                infoWebEngine.loadContent(html);
            });
        } catch (HPOException e) {
            PopUps.showException("Error","Could not retrieve HPO Stats",e);
        }
    }

    @FXML
    private void showMondoStatistics(ActionEvent e) {
        e.consume();
        MondoStats stats = new MondoStats(optionalResources.getMondoOntology());
        String html = MondoStatsHtmlGenerator.getHTML(stats);
        Platform.runLater(() -> {
            infoWebEngine = hpoAnalysisWebView.getEngine();
            infoWebEngine.loadContent(html);
        });
    }


    @FXML
    private void showEntriesNeedingMoreAnnotations(ActionEvent e) {
        e.consume();
        AnnotationTlc tlc = new AnnotationTlc(optionalResources.getHpoOntology(), optionalResources.getDisease2AnnotationMap());
        String html = AnnotationTlcHtmlGenerator.getHTML(tlc);
        Platform.runLater(() -> {
            infoWebEngine = hpoAnalysisWebView.getEngine();
            infoWebEngine.loadContent(html);
        });
    }

    @FXML
    private void showEntriesNeedingMoreSpecificAnnotation(ActionEvent e) {
        e.consume();
        AnnotationTlc tlc = new AnnotationTlc(optionalResources.getHpoOntology(), optionalResources.getDisease2AnnotationMap());
        String html = AnnotationTlcHtmlGenerator.getHTMLSpecificTerms(tlc);
        Platform.runLater(() -> {
            infoWebEngine = hpoAnalysisWebView.getEngine();
            infoWebEngine.loadContent(html);
        });
    }


    @FXML
    private void countNewEntries(ActionEvent e) {
        e.consume();

        final String pattern = "yyyy-MM-dd";
        final DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern(pattern);
        StringConverter<LocalDate> converter = new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };
        datepicker.setConverter(converter);
        datepicker.setPromptText(pattern.toLowerCase());

        String date = datepicker.getValue().format(DateTimeFormatter.ofPattern(pattern));
        LocalDate d = datepicker.getValue();
        System.out.println("Count new entries: " + date);


        String annotPath = optionalResources.getAnnotationPath();
        System.out.println("annotPath: " + annotPath);

        int annot_count=0;
        int newannot_count=0;
        int bad_parse = 0;
        Pattern datepattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
        Map<LocalDate,Integer> counter = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(annotPath));
            String line;
            while ((line=br.readLine())!= null) {
                if (line.startsWith("#") || line.startsWith("DatabaseID")) continue; // header
                if (line.startsWith("ORPHA")) continue; // we will not count ORPHA
                //BiocurationBy is the 12th field
                annot_count++;
                String []fields = line.split("\t");
                if (fields.length<12) {
                    bad_parse++;
                    continue;
                }
                String biocurated = fields[11];
                Matcher matcher = datepattern.matcher(biocurated);
                while (matcher.find()) {
                    String m = matcher.group(1);
                    LocalDate curationData =  LocalDate.parse(m,dateFormatter);
                    if (curationData.isAfter(d)) {
                        newannot_count++;
                        System.out.println("target: "+ d + ", new="+ curationData);
                        counter.putIfAbsent(curationData,0);
                        Integer count = 1 + counter.get(curationData);
                        counter.put(curationData,count);
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            PopUps.showException("Error","Could not read annotations to count new entries",ex);
            return;
        }
        int oldannotcount = annot_count - newannot_count;
        System.out.println("total annot: " + annot_count + ", new: "
                + newannot_count + ", old: "+ oldannotcount);
        String html = AnnotationTlcHtmlGenerator.getNewAnnotationHTML(d, annot_count, newannot_count, oldannotcount, counter);
        Platform.runLater(() -> {
            infoWebEngine = hpoAnalysisWebView.getEngine();
            infoWebEngine.loadContent(html);
        });

    }


}
