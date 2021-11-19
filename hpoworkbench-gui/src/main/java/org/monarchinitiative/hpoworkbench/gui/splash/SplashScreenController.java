package org.monarchinitiative.hpoworkbench.gui.splash;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.monarchinitiative.hpoworkbench.StartupTask;
import org.monarchinitiative.hpoworkbench.resources.OptionalHpoResource;
import org.monarchinitiative.hpoworkbench.resources.OptionalHpoaResource;
import org.monarchinitiative.hpoworkbench.resources.OptionalMondoResource;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

@Component
public class SplashScreenController implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SplashScreenController.class);
    @FXML
    private Label progress;

    public static Label label;

    private final OptionalHpoResource optionalHpoResource;

    private final OptionalMondoResource optionalMondoResource;

    private final OptionalHpoaResource optionalHpoaResource;
    private final ExecutorService executor;
    /**
     * Application-specific properties (not the System properties!) defined in the 'application.properties' file that
     * resides in the classpath.
     */
    private final Properties pgProperties;

    private boolean success;

    @Autowired
    public SplashScreenController(OptionalHpoResource optionalHpoResource,
                                  OptionalMondoResource optionalMondoResource,
                                  OptionalHpoaResource optionalHpoaResource,
                                  @Qualifier("configProperties") Properties properties,
                                  ExecutorService executorService) {
        this.optionalHpoResource = optionalHpoResource;
        this.optionalMondoResource = optionalMondoResource;
        this.optionalHpoaResource = optionalHpoaResource;
        this.pgProperties = properties;
        this.executor = executorService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        label = progress;
        LOGGER.info("Initializing splash screen");
        StartupTask task = new StartupTask(optionalHpoResource, optionalMondoResource, optionalHpoaResource, pgProperties);
        task.setOnSucceeded(e -> publishMessage(true));
        task.setOnFailed(e -> publishMessage(false));
        this.executor.submit(task);
    }

    private void publishMessage(boolean successfully_loaded_files) {
        success = successfully_loaded_files;
    }

    @FXML
    private void handleButtonAction(ActionEvent e) {

    }
}
