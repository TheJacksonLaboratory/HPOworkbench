package org.monarchinitiative.hpoworkbench.gui.webviewerutil;

import javafx.stage.Stage;

public class WebViewerFactory {

    private WebViewerFactory() {

    }




    public static WebViewerPopup hpoStats(String html, Stage stage) {
        String title = "HPO Stats";
        return new SimpleWebViewerPopup(title, html, stage);
    }

    public static WebViewerPopup mondoStats(String html, Stage stage) {
        String title = "Mondo Stats";
        return new SimpleWebViewerPopup(title, html, stage);
    }

    public static WebViewerPopup entriesNeedingMoreAnnotations(String html, Stage stage) {
        String title = "HPO disease entries that may need additional annotations";
        return new SimpleWebViewerPopup(title, html, stage);
    }

    public static WebViewerPopup entriesNeedingSpecificAnnotations(String html, Stage stage) {
        String title = "HPO disease entries that may need more specific annotations";
        return new SimpleWebViewerPopup(title, html, stage);
    }
}
