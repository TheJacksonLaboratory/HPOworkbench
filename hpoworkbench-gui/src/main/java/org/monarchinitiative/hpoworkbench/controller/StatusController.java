package org.monarchinitiative.hpoworkbench.controller;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ResourceBundle;



/**
 * This class is a controller of the bottom part of the main dialog window. Status messages are displayed here, as
 * well as the copyright.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @version 0.1.10
 * @since 0.1
 */
@Component
public final class StatusController {

    private static final int MAX_MESSAGES = 1;

    private final OptionalResources optionalResources;



    @Autowired
    StatusController(OptionalResources optionalResources) {
        this.optionalResources = optionalResources;
    }

    /**
     * This method is run after FXMLLoader injected all FXML elements.
     */
    @FXML
    public void initialize() {

    }


}
