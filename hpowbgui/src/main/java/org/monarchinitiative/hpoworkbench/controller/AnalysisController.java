package org.monarchinitiative.hpoworkbench.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.analysis.AnnotationTlc;
import org.monarchinitiative.hpoworkbench.analysis.HpoStats;
import org.monarchinitiative.hpoworkbench.analysis.MondoStats;
import org.monarchinitiative.hpoworkbench.exception.HPOException;
import org.monarchinitiative.hpoworkbench.gui.PopUps;
import org.monarchinitiative.hpoworkbench.html.AnnotationTlcHtmlGenerator;
import org.monarchinitiative.hpoworkbench.html.HpoStatsHtmlGenerator;
import org.monarchinitiative.hpoworkbench.html.MondoStatsHtmlGenerator;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.monarchinitiative.phenol.ontology.data.ImmutableTermId;
import org.monarchinitiative.phenol.ontology.data.TermId;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.Properties;

public final class AnalysisController {
    private static final Logger logger = LogManager.getLogger();

    private final OptionalResources optionalResources;

    /**
     * WebView for displaying details of the analysis.
     */
    @FXML
    private WebView hpoAnalysisWebView;

    /**
     * WebEngine backing up the {@link #hpoAnalysisWebView}.
     */
    private WebEngine infoWebEngine;

    private final TermId MONDO_ROOT_ID = ImmutableTermId.constructWithPrefix("MONDO:0000001");

    /**
     * Unused, but still required.
     */
    private final File hpoWorkbenchDir;
    /**
     * Application-specific properties (not the System properties!) defined in the 'application.properties' file that
     * resides in the classpath.
     */
    private final Properties properties;
    /**
     * Reference to the primary stage of the App.
     */
    private final Stage primaryStage;

    @Inject
    public AnalysisController(OptionalResources optionalResources, Properties properties,
                              @Named("mainWindow") Stage primaryStage, @Named("hpoWorkbenchDir") File hpoWorkbenchDir) {
        this.optionalResources = optionalResources;
        this.properties = properties;
        this.primaryStage = primaryStage;
        this.hpoWorkbenchDir = hpoWorkbenchDir;
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


}
