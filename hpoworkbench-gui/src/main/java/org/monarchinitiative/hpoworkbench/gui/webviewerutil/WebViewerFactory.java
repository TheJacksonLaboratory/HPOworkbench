package org.monarchinitiative.hpoworkbench.gui.webviewerutil;

import javafx.stage.Stage;

public class WebViewerFactory {

    private WebViewerFactory() {

    }




    public static WebViewerPopup hpoStats(String html, Stage stage) {
        String title = "HPO Stats";
        WebViewerPopup popup = new SimpleWebViewerPopup(title, html, stage);
        return popup;
    }

    public static WebViewerPopup mondoStats(String html, Stage stage) {
        String title = "Mondo Stats";
        WebViewerPopup popup = new SimpleWebViewerPopup(title, html, stage);
        return popup;
    }

    public static WebViewerPopup entriesNeedingMoreAnnotations(String html, Stage stage) {
        String title = "HPO disease entries that may need additional annotations";
        WebViewerPopup popup = new SimpleWebViewerPopup(title, html, stage);
        return popup;
    }

    public static WebViewerPopup entriesNeedingSpecificAnnotations(String html, Stage stage) {
        String title = "HPO disease entries that may need more specific annotations";
        WebViewerPopup popup = new SimpleWebViewerPopup(title, html, stage);
        return popup;
    }
}
