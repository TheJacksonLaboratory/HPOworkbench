package org.monarchinitiative.hpoworkbench.io;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.monarchinitiative.hpoworkbench.config.ApplicationProperties;
import org.monarchinitiative.hpoworkbench.exception.HPOWorkbenchException;
import org.monarchinitiative.hpoworkbench.gui.PlatformUtil;
import org.monarchinitiative.hpoworkbench.gui.PopUps;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Properties;

@Component
public class DownloaderFactory {
    Logger LOGGER = LoggerFactory.getLogger(DownloaderFactory.class);

    private final OptionalResources optionalResources;

    private final File hpoWebConfigurationDirectory;



    private final Properties pgProperties;

    private final  ApplicationProperties applicationProperties;

    @Autowired
    public DownloaderFactory(OptionalResources optres,
                             ApplicationProperties applicationProperties,
                             Properties pgProperties,
                             File appHomeDir) {
        this.optionalResources = optres;
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
            DirectIndirectHpoAnnotationParser parser = new DirectIndirectHpoAnnotationParser(hpoAnnotationsFileName, optionalResources
                    .getHpoOntology());
            optionalResources.setDirectAnnotMap(parser.getDirectAnnotMap());
            optionalResources.setIndirectAnnotMap(parser.getTotalAnnotationMap());
            pgProperties.setProperty(OptionalResources.HPOA_PATH_PROPERTY, hpoAnnotationsFileName);
        });
        hpodownload.setOnFailed(event -> {
            window.close();
            LOGGER.error("Unable to download phenotype_annotation.tab file");
            optionalResources.setIndirectAnnotMap(null);
            optionalResources.setDirectAnnotMap(null);
            pgProperties.remove(OptionalResources.HPOA_PATH_PROPERTY);
        });
        Thread thread = new Thread(hpodownload);
        thread.start();
    }

    public void downloadMondo() {
        ProgressIndicator pb = new ProgressIndicator();
        String mondoUrl = applicationProperties.getMondoJsonUrl();
        if (mondoUrl == null) {
            // should never happen
            PopUps.showInfoMessage("Could not find key for Mondo JSON URL in properties file", "Error");
            return;
        }
        Stage window = setupWindow("Mondo JSON download", "downloading mondo.json...", pb);

        Task<Void> mondodownload = new Downloader(hpoWebConfigurationDirectory, mondoUrl, PlatformUtil.MONDO_JSON_FILENAME);
        pb.progressProperty().bind(mondodownload.progressProperty());

        window.show();
        mondodownload.setOnSucceeded(event -> {
            window.close();
            LOGGER.trace(String.format("Successfully downloaded mondo to %s", hpoWebConfigurationDirectory));
            String mondoJsonPath = hpoWebConfigurationDirectory + File.separator + PlatformUtil.MONDO_JSON_FILENAME;
            Ontology mondo =  OntologyLoader.loadOntology(new File(mondoJsonPath));
            optionalResources.setMondoOntology(mondo);
            pgProperties.setProperty(OptionalResources.MONDO_PATH_PROPERTY, mondoJsonPath);

        });
        mondodownload.setOnFailed(event -> {
            window.close();
            LOGGER.error("Unable to download MONDO json file");
            optionalResources.setMondoOntology(null);
            pgProperties.remove(OptionalResources.MONDO_PATH_PROPERTY);
        });
        Thread thread = new Thread(mondodownload);
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
            optionalResources.setHpoOntology(hpo);
            pgProperties.setProperty(OptionalResources.HP_JSON_PATH_PROPERTY, hpoJsonPath);

        });
        hpodownload.setOnFailed(event -> {
            window.close();
            LOGGER.error("Unable to download HPO json file");
            optionalResources.setHpoOntology(null);
            pgProperties.remove(OptionalResources.HP_JSON_PATH_PROPERTY);
        });
        Thread thread = new Thread(hpodownload);
        thread.start();
    }


}
