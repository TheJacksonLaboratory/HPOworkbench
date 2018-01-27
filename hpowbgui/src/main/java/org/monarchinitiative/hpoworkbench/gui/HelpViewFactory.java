package org.monarchinitiative.hpoworkbench.gui;

/*
 * #%L
 * HPhenote
 * %%
 * Copyright (C) 2017 Peter Robinson
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;


/**
 * A helper class that displays the Help in a JavaFX webview browser
 * @author Peter Robinson
 * @version 0.1.3 (2017-11-12)
 */
public class HelpViewFactory {
    private static final Logger logger = LogManager.getLogger();
    private static final String READTHEDOCS_SITE = "http://hpo-workbench.readthedocs.io/en/latest/";

    private static String getHTML() {
        String sb = "<html><body>\n" +
                inlineCSS() +
                "<h1>HPO Workbench Help</h1>" +
                "<p><i>HPO Workbench</i> is designed to help curators explore the HPO and suggest new terms, " +
                "annotations, or corrections to current content.</p>" +
                setup() +
                openFile() +
                "</body></html>";
        return sb;

    }


    private static String inlineCSS() {
        return "<head><style>\n" +
                "  html { margin: 0; padding: 0; }" +
                "body { font: 100% georgia, sans-serif; line-height: 1.88889;color: #001f3f; margin: 10; padding: 10; }"+
                "p { margin-top: 0;text-align: justify;}"+
                "h2,h3 {font-family: 'serif';font-size: 1.4em;font-style: normal;font-weight: bold;"+
                "letter-spacing: 1px; margin-bottom: 0; color: #001f3f;}"+
                "  </style></head>";
    }

    private static String setup() {
        return "<h2>Setup</h2>" +
                "<p>When you use HPO Workbench for the first time, you need to download the HPO ontology file" +
                "and the disease annotations from the Edit menu. HPO will download these files to you user" +
                "directory. To get an update simply download again.</p>\n";
    }

    private static String openFile() {
        return String.format("<h2>Working with HPO Workbench</h2>" +
                "<p>A tutorial and detailed documentation for HPO Workbench can be found at readthedocs: %s</p>",READTHEDOCS_SITE);
    }




    /** Open a dialog that provides concise help for using PhenoteFX. */
    public static void openHelpDialog() {
        Stage window;
        String windowTitle = "HPO Workbench Help";
        window = new Stage();
        window.setOnCloseRequest( event -> {window.close();} );
        window.setTitle(windowTitle);


        Pane pane = new Pane();
        VBox vbox =  new VBox();
        vbox.setPrefHeight(600);
        vbox.setPrefWidth(800);
        WebView wview = new WebView();
        wview.getEngine().loadContent(getHTML());

        pane.getChildren().add(vbox);
        HBox hbox = new HBox();
        hbox.setPrefHeight(30);
        hbox.setPrefWidth(800);
        Region region=new Region();
        region.setPrefHeight(30);
        region.setPrefWidth(400);
        hbox.setHgrow(region, Priority.ALWAYS);


        Button openRTDbutton = new Button("Open ReadTheDocs");
        openRTDbutton.setOnAction( e-> openBrowser());

        Button button = new Button("Close");
        button.setOnAction( e->window.close());
        hbox.getChildren().addAll(region,openRTDbutton,button);
        vbox.getChildren().addAll(wview,hbox);
        Scene scene = new Scene(pane, 800, 600);

        window.setScene(scene);
        window.showAndWait();
    }


    /**
     * Open a JavaFW Webview window and confirmDialog our read the docs help documentation in it.
     */
    public static void openBrowser() {
        try{
            Stage window;
            window = new Stage();
            WebView web = new WebView();
            web.getEngine().load(READTHEDOCS_SITE);
            Scene scene = new Scene(web);
            window.setScene(scene);
            window.show();
        } catch (Exception e){
            logger.error(String.format("Could not open browser to show RTD: %s",e.toString()));
            e.printStackTrace();
        }
    }

}
