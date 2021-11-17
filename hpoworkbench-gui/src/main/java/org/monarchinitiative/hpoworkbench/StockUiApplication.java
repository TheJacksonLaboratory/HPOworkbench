package org.monarchinitiative.hpoworkbench;


import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;


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

