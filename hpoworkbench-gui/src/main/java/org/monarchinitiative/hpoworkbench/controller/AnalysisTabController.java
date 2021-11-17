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
import org.monarchinitiative.hpoworkbench.model.HpoWbModel;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.ontology.data.Ontology;
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

    private HpoWbModel hpoWbModel;


    @Autowired
    public AnalysisTabController(HpoWbModel hpoWbModel,
                                 @Qualifier("appHomeDir") File hpoWorkbenchDir) {
         File hpoWorkbenchDir1 = hpoWorkbenchDir;
         this.hpoWbModel = hpoWbModel;
    }


    @FXML
    public void initialize() {
        logger.info("Initialize analysis tab");
    }


    @FXML
    private void showHpoStatistics() {
        Optional<Ontology> opt = hpoWbModel.getHpo();
        if (opt.isEmpty()) {
            logger.error("Attempt to show HPO stats before initializing HPO ontology object");
            return;
        }
        Ontology hpo = opt.get();
        try {
            HpoStats stats = new HpoStats(hpo, hpoWbModel.getId2diseaseMap());
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
        Optional<Ontology> opt = hpoWbModel.getMondo();
        if (opt.isEmpty()) {
            logger.error("Attempt to show Mondo stats with null Mondo object");
        }
        MondoStats stats = new MondoStats(opt.get());
        String html = MondoStatsHtmlGenerator.getHTML(stats);
        Platform.runLater(() -> {
            infoWebEngine = hpoAnalysisWebView.getEngine();
            infoWebEngine.loadContent(html);
        });
    }


    @FXML
    private void showEntriesNeedingMoreAnnotations(ActionEvent e) {
        e.consume();
        Optional<Ontology> opt = hpoWbModel.getHpo();
        if (opt.isEmpty()) {
            logger.error("null HPO object");
            return;
        }
        Ontology hpo = opt.get();
        AnnotationTlc tlc = new AnnotationTlc(hpo, hpoWbModel.getId2diseaseMap());
        String html = AnnotationTlcHtmlGenerator.getHTML(tlc);
        Platform.runLater(() -> {
            infoWebEngine = hpoAnalysisWebView.getEngine();
            infoWebEngine.loadContent(html);
        });
    }

    @FXML
    private void showEntriesNeedingMoreSpecificAnnotation(ActionEvent e) {
        e.consume();
        Optional<Ontology> opt = hpoWbModel.getHpo();
        if (opt.isEmpty()) {
            logger.error("null HPO object");
            return;
        }
        Ontology hpo = opt.get();
        AnnotationTlc tlc = new AnnotationTlc(hpo, hpoWbModel.getId2diseaseMap());
        String html = AnnotationTlcHtmlGenerator.getHTMLSpecificTerms(tlc);
        Platform.runLater(() -> {
            infoWebEngine = hpoAnalysisWebView.getEngine();
            infoWebEngine.loadContent(html);
        });
    }
}
