package org.monarchinitiative.hpoworkbench;

import com.sun.javafx.application.LauncherImpl;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.monarchinitiative.hpoworkbench.gui.splash.HpoWbPreloader;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Properties;


public class HpoWorkbenchApplication extends Application {
    private ConfigurableApplicationContext applicationContext;

    static public final String HPOWB_NAME_KEY = "hpowb.name";

    static public final String HPOWB_VERSION_PROP_KEY = "hpowb.version";


    @Override
    public void start(Stage stage) throws InterruptedException {
        //loadSplashScreen();
        // notifyPreloader(new HpoWbPreloader.StateChangeNotification(HpoWbPreloader.StateChangeNotification.Type.BEFORE_START));
        // Perform some heavy lifting (i.e. database start, check for application updates, etc. )
//        for (int i = 1; i <= 10; i++) {
//            double progress =(double) i/10;
//            System.out.println("progress: " +  progress);
//            LauncherImpl.notifyPreloader(this, new HpoWbPreloader.ProgressNotification(progress));
////            try {
////                Thread.sleep(1000);
////            }catch (Exception e) {
////                e.printStackTrace();
////            }
//        }

        applicationContext.publishEvent(new StageReadyEvent(stage));
    }

    @Override
    public void init() {

        applicationContext = new SpringApplicationBuilder(StockUiApplication.class).run();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        final Properties pgProperties = applicationContext.getBean("configProperties", Properties.class);
        final File configFile = applicationContext.getBean("configFilePath", File.class);
        try (OutputStream os = Files.newOutputStream(configFile.toPath())) {
            pgProperties.store(os, "HpoWorkbench properties");
        }
        Platform.exit();
        applicationContext.close();
    }




    static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) {
            super(stage);
        }

        public Stage getStage() {
            return ((Stage) getSource());
        }
    }

    static void loadSplashScreen()  {
        Stage splashStage = new Stage();
        ClassPathResource splashResource = new ClassPathResource("fxml/splashScreen.fxml");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(splashResource.getURL());
            Parent splashRoot = fxmlLoader.load();
            Scene splashScene = new Scene(splashRoot);
            splashStage.setScene(splashScene);
            splashStage.initStyle(StageStyle.UNDECORATED);
            splashStage.show();

            setFadeInOut(splashRoot, splashStage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void setFadeInOut(Parent splashScene, Stage splashStage) {
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(3), splashScene);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setCycleCount(1);

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), splashScene);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setCycleCount(1);
        fadeIn.play();

        fadeIn.setOnFinished((e) -> fadeOut.play());
        fadeOut.setOnFinished((e) -> splashStage.close());
    }

}
