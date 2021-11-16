package org.monarchinitiative.hpoworkbench;


import com.sun.javafx.application.LauncherImpl;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.monarchinitiative.hpoworkbench.gui.splash.HpoWbPreloader;
import org.monarchinitiative.hpoworkbench.gui.splash.SplashScreenController;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;


/**
 * Main class of the HPOworkbench GUI app
 * @author Peter N Robinson
 */
@SpringBootApplication
public class StockUiApplication {

    public static void main(String[] args) {
        Application.launch(HpoWorkbenchApplication.class, args);
    }



}

