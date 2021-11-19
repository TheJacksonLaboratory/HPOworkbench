package org.monarchinitiative.hpoworkbench.gui.webviewerutil;

import javafx.stage.Stage;

public class SimpleWebViewerPopup extends WebViewerPopup {
    private final String html;
    private final String title;
    public SimpleWebViewerPopup(String title, String html, Stage stage) {
        super(stage);
        this.html = html;
        this.title = title;
    }

    @Override
    public void popup() {
        showHtmlContent(title, html);
    }
}
