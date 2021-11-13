package org.monarchinitiative.hpoworkbench;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;


public class HpoWorkbenchApplication extends Application {
    private ConfigurableApplicationContext applicationContext;

    static public final String HPOWB_NAME_KEY = "hpowb.name";

    static public final String HPOWB_VERSION_PROP_KEY = "hpowb.version";

    @Autowired
    @Qualifier("configProperties")
    private Properties pgProperties;

    @Override
    public void start(Stage stage) {
        applicationContext.publishEvent(new StageReadyEvent(stage));
    }

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(StockUiApplication.class).run();
        // export app's version into System properties
        try (InputStream is = getClass().getResourceAsStream("/application.properties")) {
            Properties properties = new Properties();
            properties.load(is);
            String version = properties.getProperty(HPOWB_VERSION_PROP_KEY, "unknown version");
            System.setProperty(HPOWB_VERSION_PROP_KEY, version);
            String name = properties.getProperty(HPOWB_NAME_KEY, "Fenominal");
            System.setProperty(HPOWB_NAME_KEY, name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File f = applicationContext.getBean("appHomeDir", File.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        //final Properties pgProperties = applicationContext.getBean("pgProperties", Properties.class);
        final File configFile = applicationContext.getBean("appHomeDir", File.class);
        try (OutputStream os = Files.newOutputStream(configFile.toPath())) {
            pgProperties.store(os, "HpoWorkbench properties");
        }
        Platform.exit();
        // close the context
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

}
