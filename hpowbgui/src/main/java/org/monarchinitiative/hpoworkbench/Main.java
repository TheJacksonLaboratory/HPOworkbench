package org.monarchinitiative.hpoworkbench;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.controller.MainController;
import org.monarchinitiative.hpoworkbench.gui.PlatformUtil;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;


/**
 * The driver class of the LOINC2HPO biocuration app, which is intended to help annotate LOINC codes to the
 * relevantHPO Terms.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.2
 */

public class Main extends Application {
    private static final Logger logger = LogManager.getLogger();
    private static final String WINDOW_TITLE = "Human Phenotype Ontology Workbench";
    /** reference to the primary stage. */
    public static Stage primarystage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws IOException {
    }


    @Override
    public void start(Stage window) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        primarystage=window;

        Scene scene = new Scene(root, 1200, 800);

        window.setTitle(WINDOW_TITLE);
        window.setScene(scene);
        Image image = new Image(Main.class.getResourceAsStream("/img/icon.jpg"));
        window.getIcons().add(image);
        window.setTitle(WINDOW_TITLE);
        if (PlatformUtil.isMacintosh()) {
            try {
                URL iconURL = Main.class.getResource("/img/icon.jpg");
                java.awt.Image macimage = new ImageIcon(iconURL).getImage();
                com.apple.eawt.Application.getApplication().setDockIconImage(macimage);
            } catch (Exception e) {
                // Not for Windows or Linux. Just skip it!
            }
        }

        window.show();
        window.setOnCloseRequest(e -> Platform.exit());

    }

    public Stage getStage() {
        return primarystage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws Exception {

    }


}
