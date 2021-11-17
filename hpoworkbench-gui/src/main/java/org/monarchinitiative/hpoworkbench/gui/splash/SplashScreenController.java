package org.monarchinitiative.hpoworkbench.gui.splash;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.monarchinitiative.hpoworkbench.StartupTask;
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

    private final OptionalResources optionalResources;
    private final ExecutorService executor;
    /**
     * Application-specific properties (not the System properties!) defined in the 'application.properties' file that
     * resides in the classpath.
     */
    private final Properties pgProperties;

    private boolean success;

    @Autowired
    public SplashScreenController(OptionalResources optionalResources,
                                  @Qualifier("configProperties") Properties properties,
                                  ExecutorService executorService) {
        this.optionalResources = optionalResources;
        this.pgProperties = properties;
        this.executor = executorService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        label = progress;
        LOGGER.info("Initializing splash screen");
        StartupTask task = new StartupTask(optionalResources, pgProperties);
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
