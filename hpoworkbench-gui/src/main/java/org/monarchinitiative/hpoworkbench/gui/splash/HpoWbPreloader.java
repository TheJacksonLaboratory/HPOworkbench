package org.monarchinitiative.hpoworkbench.gui.splash;

import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.core.io.ClassPathResource;

public class HpoWbPreloader extends Preloader {

    private Stage preloaderStage;
    private Scene scene;

    @Override
    public void init() throws Exception {
        ClassPathResource splashResource = new ClassPathResource("fxml/splashScreen.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(splashResource.getURL());
        Parent splashRoot = fxmlLoader.load();
        scene = new Scene(splashRoot);

    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        this.preloaderStage = primaryStage;
        preloaderStage.setScene(scene);
        preloaderStage.initStyle(StageStyle.UNDECORATED);
        preloaderStage.show();
    }

    @Override
    public void handleApplicationNotification(Preloader.PreloaderNotification info) {
        if (info instanceof ProgressNotification progressNotification) {
            SplashScreenController.label.setText("Loading " + progressNotification.getProgress() + "%");
        }
    }

    @Override
    public void handleStateChangeNotification(Preloader.StateChangeNotification info) {
        StateChangeNotification.Type type = info.getType();
        switch (type) {
            case BEFORE_START -> {
                preloaderStage.hide();
            }

        }
    }

}
