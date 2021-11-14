package org.monarchinitiative.hpoworkbench.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.monarchinitiative.hpoworkbench.StartupTask;
import org.monarchinitiative.hpoworkbench.exception.HPOWorkbenchException;
import org.monarchinitiative.hpoworkbench.exception.HpoWorkbenchRuntimeException;
import org.monarchinitiative.hpoworkbench.gui.HelpViewFactory;
import org.monarchinitiative.hpoworkbench.gui.PlatformUtil;
import org.monarchinitiative.hpoworkbench.gui.PopUps;
import org.monarchinitiative.hpoworkbench.io.*;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Controller for HPO Workbench
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
@Component
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private final OptionalResources optionalResources;

    /**
     * Directory, where ontologies and HPO annotation files are being stored.
     */
    private final File hpoWorkbenchDir;

    /**
     * Application-specific properties (not the System properties!) defined in the 'application.properties' file that
     * resides in the classpath.
     */
    private final Properties pgProperties;

    private final ExecutorService executor;

    @Autowired
    DownloaderFactory factory;
    /**
     * Place at the bottom of the window controlled by {@link StatusController} for showing messages to user
     */
    @FXML
    public StackPane statusStackPane;


    @Autowired
    public MainController(OptionalResources optionalResources,
                          @Qualifier("configProperties") Properties properties,
                          @Qualifier("appHomeDir") File hpoWorkbenchDir,
                            ExecutorService executorService) {
        this.optionalResources = optionalResources;
        this.pgProperties = properties;
        this.hpoWorkbenchDir = hpoWorkbenchDir;
        this.executor = executorService;
    }

    @FXML
    public void initialize() {
        // NO-OP
        logger.info("Initializing main controller");
        StartupTask task = new StartupTask(optionalResources, pgProperties);
//        this.optionalResources.
//        this.hpoReadyLabel.textProperty().bind(task.messageProperty());
//        task.setOnSucceeded(e -> this.hpoReadyLabel.textProperty().unbind());
        this.executor.submit(task);
        // only enable analyze if Ontology downloaded (enabled property watches
//        this.setupButton.disableProperty().bind(optionalResources.ontologyProperty().isNull());
//        this.parseButton.setDisable(true);
//        this.previwButton.setDisable(true);
//        this.outputButton.setDisable(true);
    }

    public static String getVersion() {
        String version = "0.0.0";// default, should be overwritten by the following.
        try {
            Package p = MainController.class.getPackage();
            version = p.getImplementationVersion();
        } catch (Exception e) {
            // do nothing
        }
        if (version == null) version = "1.6.0"; // this works on a maven build but needs to be reassigned in intellij
        return version;
    }

    /**
     * This is called from the Edit menu and allows the user to import a local copy of
     * hp.obo (usually because the local copy is newer than the official release version of hp.obo).
     *
     * @param e event
     */
    @FXML
    private void importLocalHpObo(ActionEvent e) {
        e.consume();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import local hp.obo file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HPO OBO file (*.obo)", "*.obo");
        chooser.getExtensionFilters().add(extFilter);
        Stage stage = (Stage)((Node) e.getSource()).getScene().getWindow();
        File f = chooser.showOpenDialog(stage);
        if (f == null) {
            logger.error("Unable to obtain path to local HPO OBO file");
            PopUps.showInfoMessage("Unable to obtain path to local HPO OBO file", "Error");
            return;
        }
        String hpoOboPath = f.getAbsolutePath();

        HPOParser parser = new HPOParser(hpoOboPath);
        optionalResources.setHpoOntology(parser.getHPO());
        pgProperties.setProperty("hpo.obo.path", hpoOboPath);
    }

    @FXML
    private void close(ActionEvent e) {
        logger.trace("Closing down");
        Platform.exit();
    }

    @FXML
    private void downloadHPO(ActionEvent e) {
        factory.downloadHpoJson();
        e.consume();
    }

    @FXML
    private void downloadMondo(ActionEvent e) {
        factory.downloadMondo();
        e.consume();
    }

    @FXML
    private void downloadHPOAnnotations(ActionEvent e) {
        factory.downloadHPOAnnotations();
        e.consume();
    }



    /**
     * Show the help dialog
     */
    @FXML
    private void helpWindow(ActionEvent e) {
        HelpViewFactory.openBrowser();
        e.consume();
    }

    /** Show the about message */
    @FXML private void aboutWindow(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("HPO Workbench");
        alert.setHeaderText("Human Phenotype Ontology Workbench");
        String s = "A tool for working with the HPO.\n\u00A9 Monarch Initiative 2021";
        alert.setContentText(s);
        alert.showAndWait();
        e.consume();
    }


    /**
     * Determines the behavior of the app. Are we browsing HPO terms, diseases, or suggesting new annotations?
     */
    enum mode {
        BROWSE_HPO, BROWSE_DISEASE, NEW_ANNOTATION
    }

}
