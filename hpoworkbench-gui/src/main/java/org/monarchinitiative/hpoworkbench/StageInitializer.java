package org.monarchinitiative.hpoworkbench;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Component
public class StageInitializer implements ApplicationListener<HpoWorkbenchApplication.StageReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageInitializer.class);

    private final String applicationTitle;

    private final ApplicationContext applicationContext;


    public StageInitializer(@Value("${spring.application.ui.title}") String applicationTitle, ApplicationContext context) {
        this.applicationTitle = applicationTitle;
        this.applicationContext = context;
    }


    @Override
    public void onApplicationEvent(HpoWorkbenchApplication.StageReadyEvent event) {
        try {
            ClassPathResource hpowbResource = new ClassPathResource("fxml/main.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(hpowbResource.getURL());
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent parent = fxmlLoader.load();
            Stage stage = event.getStage();
            stage.setScene(new Scene(parent, 1200, 900));
            stage.setResizable(true);
            stage.setTitle(applicationTitle);
            readAppIcon().ifPresent(stage.getIcons()::add);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Optional<Image> readAppIcon() {
        try (InputStream is = StageInitializer.class.getResourceAsStream("/img/phenomenon.png")) {
            if (is != null) {
                return Optional.of(new Image(is));
            }
        } catch (IOException e) {
            LOGGER.warn("Error reading app icon {}", e.getMessage());
        }
        return Optional.empty();
    }
}
