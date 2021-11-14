package org.monarchinitiative.hpoworkbench.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.monarchinitiative.hpoworkbench.StartupTask;
import org.monarchinitiative.hpoworkbench.exception.HPOWorkbenchException;
import org.monarchinitiative.hpoworkbench.exception.HpoWorkbenchRuntimeException;
import org.monarchinitiative.hpoworkbench.gui.HelpViewFactory;
import org.monarchinitiative.hpoworkbench.gui.PlatformUtil;
import org.monarchinitiative.hpoworkbench.gui.PopUps;
import org.monarchinitiative.hpoworkbench.gui.webpopup.SettingsPopup;
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
    private Label copyrightLabel;

    @FXML
    public HBox statusHBox;


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
        logger.info("Initializing main controller");
        StartupTask task = new StartupTask(optionalResources, pgProperties);
        task.setOnSucceeded(e -> publishMessage("Successfully loaded files"));
        task.setOnFailed(e -> publishMessage("Unable to load ontologies/annotations", MessageType.ERROR));
        this.executor.submit(task);
        String ver = MainController.getVersion();
        copyrightLabel.setText("HPO Workbench, v. " + ver + ", \u00A9 Monarch Initiative 2021");

        ChangeListener<? super Object> listener = (obs, oldval, newval) -> checkAll();

        optionalResources.hpoOntologyProperty().addListener(listener);
        optionalResources.indirectAnnotMapProperty().addListener(listener);
        optionalResources.directAnnotMapProperty().addListener(listener);
        optionalResources.mondoOntologyProperty().addListener(listener);
        logger.info("Done initialization");
        checkAll();
    }

    /**
     * Check availability of tracked resources and publish an appropriate message.
     */
    private void checkAll() {
        if (optionalResources.getHpoOntology() == null) { // hpo obo file is missing
            publishMessage("hpo json file is missing", MessageType.ERROR);
        } else if (optionalResources.getAnnotationPath() == null ) {
            publishMessage("phenotype.hpoa file is missing", MessageType.ERROR);
        } else if (optionalResources.getMondoOntology() == null) {
            publishMessage("Mondo file missing", MessageType.ERROR);
        } else { // since we check only 2 resources, we should be
            // fine here
            publishMessage("Ready to go", MessageType.INFO);
        }
    }


    /**
     * Post information message to the status bar.
     *
     * @param msg String with message to be displayed
     */
    void publishMessage(String msg) {
        publishMessage(msg, MessageType.INFO);
    }

    /**
     * Post the message to the status bar. Color of the text is determined by the message <code>type</code>.
     *
     * @param msg  String with message to be displayed
     * @param type message type
     */
    private void publishMessage(String msg, MessageType type) {
        int MAX_MESSAGES = 1;
        Platform.runLater(()->{
            if (statusHBox.getChildren().size() == MAX_MESSAGES) {
                statusHBox.getChildren().remove(MAX_MESSAGES - 1);
            }
            Label label = prepareContainer(type);
            label.setText(msg);
            statusHBox.getChildren().add(0, label);
        });
    }

    /**
     * Make label for displaying message in the {@link #statusHBox}. The style of the text depends on given
     * <code>type</code>
     *
     * @param type of the message to be displayed
     *
     * @return {@link Label} styled according to the message type
     */
    private Label prepareContainer(MessageType type) {
        Label label = new Label();
        label.setPrefHeight(30);
        HBox.setHgrow(label, Priority.ALWAYS);
        label.setPadding(new Insets(5));
        switch (type) {
            case WARNING:
                label.setStyle("-fx-text-fill: orange; -fx-font-weight: bolder");
                break;
            case ERROR:
                label.setStyle("-fx-text-fill: red; -fx-font-weight: bolder");
                break;
            case INFO:
            default:
                label.setStyle("-fx-text-fill: black; -fx-font-weight: bolder");
                break;
        }


        return label;
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

    @FXML
    private void showSettings(ActionEvent e) {
        Stage stage = (Stage) this.statusHBox.getScene().getWindow();
        SettingsPopup popup = new SettingsPopup(pgProperties, optionalResources, stage);
        popup.popup();
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
