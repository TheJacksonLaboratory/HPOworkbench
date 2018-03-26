package org.monarchinitiative.hpoworkbench;


import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.gui.PlatformUtil;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;


/**
 * The driver class of the LOINC2HPO biocuration app, which is intended to help annotate LOINC codes to the
 * relevantHPO Terms.
 *
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 * @version 0.1.2
 */

public class    Main extends Application {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String WINDOW_TITLE = "Human Phenotype Ontology Workbench";

    private Injector injector;

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage window) throws Exception {
        injector = Guice.createInjector(new HpoWorkbenchGuiModule(window));
        ResourceBundle bundle = injector.getInstance(ResourceBundle.class);
        Parent rootNode = FXMLLoader.load(Main.class.getResource("/fxml/main.fxml"), bundle,
                new JavaFXBuilderFactory(), injector::getInstance);

        window.setScene(new Scene(rootNode, 1200, 800));

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

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        injector.getInstance(ExecutorService.class).shutdown();

        // save properties
        Properties properties = injector.getInstance(Properties.class);
        File where = injector.getInstance(Key.get(File.class, Names.named("propertiesFilePath")));
        properties.store(new FileWriter(where), "HPOworkbench properties");
        LOGGER.trace("Properties saved to {}", where.getAbsolutePath());
    }


}
