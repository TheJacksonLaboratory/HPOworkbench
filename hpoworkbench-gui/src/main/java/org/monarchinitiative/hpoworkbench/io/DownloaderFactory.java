package org.monarchinitiative.hpoworkbench.io;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.monarchinitiative.hpoworkbench.config.ApplicationProperties;
import org.monarchinitiative.hpoworkbench.gui.PlatformUtil;
import org.monarchinitiative.hpoworkbench.gui.PopUps;
import org.monarchinitiative.hpoworkbench.resources.OptionalHpoResource;
import org.monarchinitiative.hpoworkbench.resources.OptionalHpoaResource;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Properties;

import static org.monarchinitiative.hpoworkbench.resources.OptionalHpoaResource.HPOA_PATH_PROPERTY;

@Component
public class DownloaderFactory {
    Logger LOGGER = LoggerFactory.getLogger(DownloaderFactory.class);

    private final OptionalHpoResource optionalHpoResources;

    private final OptionalHpoaResource optionalHpoaResource;

    private final File hpoWebConfigurationDirectory;



    private final Properties pgProperties;

    private final  ApplicationProperties applicationProperties;

    @Autowired
    public DownloaderFactory(OptionalHpoResource optres,
                             OptionalHpoaResource optionalHpoaResource,
                             ApplicationProperties applicationProperties,
                             Properties pgProperties,
                             File appHomeDir) {
        this.optionalHpoResources = optres;
        this.optionalHpoaResource = optionalHpoaResource;
        this.hpoWebConfigurationDirectory = appHomeDir;
        this.pgProperties = pgProperties;
        this.applicationProperties = applicationProperties;
    }


    private Stage setupWindow(String title, String labl, ProgressIndicator pb) {

        javafx.scene.control.Label label = new javafx.scene.control.Label(labl);
        FlowPane root = new FlowPane();
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.getChildren().addAll(label, pb);
        Scene scene = new Scene(root, 400, 100);
        Stage window = new Stage();
        window.setTitle(title);
        window.setScene(scene);
        return window;
    }

    public void downloadHPOAnnotations() {
        ProgressIndicator pb = new ProgressIndicator();
        String hpoaUrl = applicationProperties.getPhenotypeHpoaUrl();
        if (hpoaUrl == null) {
            // should never happen
            PopUps.showInfoMessage("Could not find key for HPOA URL in properties file", "Error");
            return;
        }
        Stage window = setupWindow("HPO annotation download", "downloading phenotype.hpoa...", pb);
        Task<Void> hpodownload = new Downloader(hpoWebConfigurationDirectory, hpoaUrl,
                PlatformUtil.HPO_ANNOTATIONS_FILENAME);
        pb.progressProperty().bind(hpodownload.progressProperty());
        window.show();
        hpodownload.setOnSucceeded(event -> {
            window.close();
            LOGGER.trace(String.format("Successfully downloaded %s to %s",
                    PlatformUtil.HPO_ANNOTATIONS_FILENAME, hpoWebConfigurationDirectory));
            String hpoAnnotationsFileName = hpoWebConfigurationDirectory + File.separator + PlatformUtil.HPO_ANNOTATIONS_FILENAME;
            DirectIndirectHpoAnnotationParser parser = new DirectIndirectHpoAnnotationParser(hpoAnnotationsFileName, optionalHpoResources
                    .getOntology());
            optionalHpoaResource.setAnnotationResources(hpoAnnotationsFileName, optionalHpoResources.getOntology());

            pgProperties.setProperty(HPOA_PATH_PROPERTY, hpoAnnotationsFileName);
        });
        hpodownload.setOnFailed(event -> {
            window.close();
            LOGGER.error("Unable to download phenotype_annotation.tab file");
            pgProperties.remove(HPOA_PATH_PROPERTY);
        });
        Thread thread = new Thread(hpodownload);
        thread.start();
    }
    public void downloadHpoJson() {
        ProgressIndicator pb = new ProgressIndicator();
        String hpoUrl = applicationProperties.getHpoJsonUrl();
        if (hpoUrl == null) {
            // should never happen
            PopUps.showInfoMessage("Could not find key for HPO JSON URL in properties file", "Error");
            return;
        }
        Stage window = setupWindow("HPO JSON download", "downloading hp.json...", pb);

        Task<Void> hpodownload = new Downloader(hpoWebConfigurationDirectory, hpoUrl, PlatformUtil.HPO_JSON_FILENAME);
        pb.progressProperty().bind(hpodownload.progressProperty());

        window.show();
        hpodownload.setOnSucceeded(event -> {
            window.close();
            LOGGER.trace(String.format("Successfully downloaded hp.json to %s", hpoWebConfigurationDirectory));
            String hpoJsonPath = hpoWebConfigurationDirectory + File.separator + PlatformUtil.HPO_JSON_FILENAME;
            Ontology hpo =  OntologyLoader.loadOntology(new File(hpoJsonPath));
            optionalHpoResources.setOntology(hpo);
            pgProperties.setProperty(OptionalHpoResource.HP_JSON_PATH_PROPERTY, hpoJsonPath);

        });
        hpodownload.setOnFailed(event -> {
            window.close();
            LOGGER.error("Unable to download HPO json file");
            optionalHpoResources.setOntology(null);
            pgProperties.remove(OptionalHpoResource.HP_JSON_PATH_PROPERTY);
        });
        Thread thread = new Thread(hpodownload);
        thread.start();
    }


}
