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

import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A helper class that displays the Help in a JavaFX webview browser
 * @author Peter Robinson
 * @version 0.1.3 (2018-01-31)
 */
public class HelpViewFactory {
    private static final Logger logger = LoggerFactory.getLogger(HelpViewFactory.class);
    private static final String READTHEDOCS_SITE = "http://hpo-workbench.readthedocs.io/en/latest/";

    private static String getHTML() {
        return "<html><body>\n" +
                inlineCSS() +
                "<h1>HPO Workbench Help</h1>" +
                "<p><i>HPO Workbench</i> is designed to help curators explore the HPO and suggest new terms, " +
                "annotations, or corrections to current content.</p>" +
                setup() +
                openFile() +
                "</body></html>";
    }

    private static String getHTMLError() {
        return "<html><body>\n" +
                inlineCSS() +
                "<h1>HPO Workbench: Connection error</h1>" +
                "<p><Unable to conect to the internet.</p>" +
                "</body></html>";
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




    /**
     * Open a JavaFW Webview window and confirmDialog our read the docs help documentation in it.
     */
    public static void openBrowser() {
        try{
            Stage window;
            window = new Stage();
            WebView web = new WebView();
            WebEngine engine=web.getEngine();
            engine.load(READTHEDOCS_SITE);
            engine.getLoadWorker().stateProperty().addListener((observable, oldvalue,newvalue)->{
                if (newvalue== Worker.State.FAILED) {
                    engine.loadContent(getHTMLError());
                }
            });
            Scene scene = new Scene(web);
            window.setScene(scene);
            window.show();
        } catch (Exception e){
            logger.error(String.format("Could not open browser to show RTD: %s", e));
            e.printStackTrace();
        }
    }

}
