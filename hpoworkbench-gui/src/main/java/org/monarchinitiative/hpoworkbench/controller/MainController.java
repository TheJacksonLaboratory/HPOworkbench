package org.monarchinitiative.hpoworkbench.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.monarchinitiative.hpoworkbench.exception.HPOWorkbenchException;
import org.monarchinitiative.hpoworkbench.gui.HelpViewFactory;
import org.monarchinitiative.hpoworkbench.gui.PlatformUtil;
import org.monarchinitiative.hpoworkbench.gui.PopUps;
import org.monarchinitiative.hpoworkbench.io.DirectIndirectHpoAnnotationParser;
import org.monarchinitiative.hpoworkbench.io.Downloader;
import org.monarchinitiative.hpoworkbench.io.HPOParser;
import org.monarchinitiative.hpoworkbench.io.MondoParser;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Properties;


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
    private final Properties properties;

    /**
     * Reference to the primary stage of the App.
     */
    private final Stage primaryStage;

    /**
     * Place at the bottom of the window controlled by {@link StatusController} for showing messages to user
     */
    @FXML
    public StackPane statusStackPane;


    @Autowired
    public MainController(OptionalResources optionalResources,
                          Properties properties,
                          @Qualifier("mainWindow") Stage primaryStage,
                          @Qualifier("hpoWorkbenchDir") File hpoWorkbenchDir) {
        this.optionalResources = optionalResources;
        this.properties = properties;
        this.primaryStage = primaryStage;
        this.hpoWorkbenchDir = hpoWorkbenchDir;
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
        File f = chooser.showOpenDialog(primaryStage);
        if (f == null) {
            logger.error("Unable to obtain path to local HPO OBO file");
            PopUps.showInfoMessage("Unable to obtain path to local HPO OBO file", "Error");
            return;
        }
        String hpoOboPath = f.getAbsolutePath();

        HPOParser parser = new HPOParser(hpoOboPath);
        optionalResources.setHpoOntology(parser.getHPO());
        properties.setProperty("hpo.obo.path", hpoOboPath);
    }

    @FXML
    private void close(ActionEvent e) {
        logger.trace("Closing down");
        Platform.exit();
    }

    @FXML
    private void downloadHPO(ActionEvent e) {
        String dirpath = hpoWorkbenchDir.getAbsolutePath();
        File f = new File(dirpath);
        if (!(f.exists() && f.isDirectory())) {
            logger.trace("Cannot download hp.obo, because directory not existing at " + f.getAbsolutePath());
            return;
        }

        ProgressIndicator pb = new ProgressIndicator();
        javafx.scene.control.Label label = new javafx.scene.control.Label("downloading hp.obo...");
        FlowPane root = new FlowPane();
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.getChildren().addAll(label, pb);
        Scene scene = new Scene(root, 400, 100);
        Stage window = new Stage();
        window.initOwner(primaryStage);
        window.setTitle("HPO download");
        window.setScene(scene);

        Task<Void> hpodownload = new Downloader(dirpath, properties.getProperty("hpo.obo.url"), PlatformUtil.HPO_OBO_FILENAME);
        pb.progressProperty().bind(hpodownload.progressProperty());
        window.show();
        hpodownload.setOnSucceeded(event -> {
            window.close();
            logger.info(String.format("Successfully downloaded hpo to %s", dirpath));
            String hpoOboPath = dirpath + File.separator + PlatformUtil.HPO_OBO_FILENAME;
            optionalResources.setHpoOntology(new HPOParser(hpoOboPath).getHPO());
            properties.setProperty("hpo.obo.path", hpoOboPath);
        });
        hpodownload.setOnFailed(event -> {
            window.close();
            logger.error("Unable to download HPO obo file");
            optionalResources.setHpoOntology(null);
            properties.remove("hpo.obo.path");
        });
        Thread thread = new Thread(hpodownload);
        thread.start();
        e.consume();
    }

    @FXML
    private void downloadMondo(ActionEvent e) {
        String dirpath = hpoWorkbenchDir.getAbsolutePath();
        File f = new File(dirpath);
        if (!(f.exists() && f.isDirectory())) {
            logger.trace("Cannot download mondo.obo, because directory does not exist at " + f.getAbsolutePath());
            return;
        }

        ProgressIndicator pb = new ProgressIndicator();
        javafx.scene.control.Label label = new javafx.scene.control.Label("downloading mondo.obo...");
        FlowPane root = new FlowPane();
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.getChildren().addAll(label, pb);
        Scene scene = new Scene(root, 400, 100);
        Stage window = new Stage();
        window.initOwner(primaryStage);
        window.setTitle("MONDO download");
        window.setScene(scene);

        String MONDO_URL =  properties.getProperty("mondo.obo.url");
        Task<Void> mondodownload = new Downloader(dirpath, MONDO_URL, PlatformUtil.MONDO_OBO_FILENAME);
        pb.progressProperty().bind(mondodownload.progressProperty());

        window.show();
        mondodownload.setOnSucceeded(event -> {
            window.close();
            logger.trace(String.format("Successfully downloaded mondo to %s", dirpath));
            String mondoOboPath = dirpath + File.separator + PlatformUtil.MONDO_OBO_FILENAME;

            MondoParser parser = new MondoParser(mondoOboPath);
            Ontology mondo = parser.getMondo();
            optionalResources.setMondoOntology(mondo);
            properties.setProperty("mondo.obo.path", mondoOboPath);

        });
        mondodownload.setOnFailed(event -> {
            window.close();
            logger.error("Unable to download MONDO obo file");
            optionalResources.setMondoOntology(null);
            properties.remove("mondo.obo.path");
        });
        Thread thread = new Thread(mondodownload);
        thread.start();
        e.consume();
    }

    @FXML
    private void downloadHPOAnnotations(ActionEvent e) {
        String dirpath = hpoWorkbenchDir.getAbsolutePath();
        File f = new File(dirpath);
        if (!(f.exists() && f.isDirectory())) {
            logger.trace("Cannot download phenotype.hpoa, because directory not existing at " + f.getAbsolutePath());
            return;
        }

        ProgressIndicator pb = new ProgressIndicator();
        javafx.scene.control.Label label = new javafx.scene.control.Label("downloading phenotype.hpoa...");
        FlowPane root = new FlowPane();
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.getChildren().addAll(label, pb);
        Scene scene = new Scene(root, 400, 100);
        Stage window = new Stage();
        window.setTitle("HPO annotation download");
        window.setScene(scene);

        Task<Void> hpodownload = new Downloader(dirpath, properties.getProperty("hpo.phenotype.annotations.url"),
                PlatformUtil.HPO_ANNOTATIONS_FILENAME);
        pb.progressProperty().bind(hpodownload.progressProperty());
        window.show();
        hpodownload.setOnSucceeded(event -> {
            window.close();
            logger.trace(String.format("Successfully downloaded %s to %s",
                    PlatformUtil.HPO_ANNOTATIONS_FILENAME, dirpath));
            String hpoAnnotationsFileName = dirpath + File.separator + PlatformUtil.HPO_ANNOTATIONS_FILENAME;
            DirectIndirectHpoAnnotationParser parser = new DirectIndirectHpoAnnotationParser(hpoAnnotationsFileName, optionalResources
                    .getHpoOntology());
            try {
                parser.doParse();
                optionalResources.setDirectAnnotMap(parser.getDirectAnnotMap());
                optionalResources.setIndirectAnnotMap(parser.getTotalAnnotationMap());
                properties.setProperty("hpo.annotations.path", hpoAnnotationsFileName);
            } catch (HPOWorkbenchException exc) {
                exc.printStackTrace(); // TODO Popup window warning
            }
        });
        hpodownload.setOnFailed(event -> {
            window.close();
            logger.error("Unable to download phenotype_annotation.tab file");
            optionalResources.setIndirectAnnotMap(null);
            optionalResources.setDirectAnnotMap(null);
            properties.remove("hpo.annotations.path");
        });
        Thread thread = new Thread(hpodownload);
        thread.start();
        e.consume();
    }

    @FXML
    public void initialize() {
        // NO-OP
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
        String s = "A tool for working with the HPO.\n\u00A9 Monarch Initiative 2018";
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