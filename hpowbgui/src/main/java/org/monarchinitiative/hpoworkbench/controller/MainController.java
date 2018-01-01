package org.monarchinitiative.hpoworkbench.controller;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.gui.PlatformUtil;
import org.monarchinitiative.hpoworkbench.io.Downloader;
import org.monarchinitiative.hpoworkbench.model.Model;

import java.io.File;

@Singleton
public class MainController {
    private static final Logger logger = LogManager.getLogger();

    /** Download address for {@code hp.obo}. */
    private final static String HP_OBO_URL ="https://raw.githubusercontent.com/obophenotype/human-phenotype-ontology/master/hp.obo";
    private Model model=null;

    @Inject
    public MainController() {
        model=new Model();
        File settings = getPathToSettingsFileAndEnsurePathExists();
        logger.trace(String.format("Setting settings file to %s",settings.getAbsolutePath()));
        model.setPathToSettingsFile(settings.getAbsolutePath());
    }



    /**
     * This function will create the .hpoworkbench directory in the user's home directory if it does not yet exist.
     * Then it will return the path of the settings file.
     * @return
     */
    private File getPathToSettingsFileAndEnsurePathExists() {
        File loinc2HpoUserDir = PlatformUtil.getHpoWorkbenchDir();
        if (!loinc2HpoUserDir.exists()) {
            File fck = new File(loinc2HpoUserDir.getAbsolutePath());
            if (!fck.mkdir()) { // make sure config directory is created, exit if not
                logger.fatal("Unable to create HPOworkbench config directory.\n"
                        + "Even though this is a serious problem I'm exiting gracefully. Bye.");
                System.exit(1);
            }
        }
        String defaultSettingsPath = PlatformUtil.getPathToSettingsFile();
        File settingsFile=new File(defaultSettingsPath);
        return settingsFile;
    }


    @FXML private void close(ActionEvent e) {
        logger.trace("Closing down");
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void downloadHPO(ActionEvent e) {
        String dirpath= PlatformUtil.getHpoWorkbenchDir().getAbsolutePath();
        File f = new File(dirpath);
        if (f==null || ! (f.exists() && f.isDirectory())) {
            logger.trace("Cannot download hp.obo, because directory not existing at " + f.getAbsolutePath());
            return;
        }
        String BASENAME="hp.obo";

        ProgressIndicator pb = new ProgressIndicator();
        javafx.scene.control.Label label=new javafx.scene.control.Label("downloading hp.obo...");
        FlowPane root = new FlowPane();
        root.setPadding(new Insets(10));
        root.setHgap(10);
        root.getChildren().addAll(label,pb);
        Scene scene = new Scene(root, 400, 100);
        Stage window = new Stage();
        window.setTitle("HPO download");
        window.setScene(scene);

        Task hpodownload = new Downloader(dirpath, HP_OBO_URL,BASENAME,pb);
        new Thread(hpodownload).start();
        window.show();
        hpodownload.setOnSucceeded(event -> {
            window.close();
            logger.trace(String.format("Successfully downloaded hpo to %s",dirpath));
            String fullpath=String.format("%s%shp.obo",dirpath,File.separator);
            model.setPathToHpOboFile(fullpath);
            model.writeSettings();
        });
        hpodownload.setOnFailed(event -> {
            window.close();
            logger.error("Unable to download HPO obo file");
        });
        Thread thread = new Thread(hpodownload);
        thread.start();

        e.consume();
    }
}
