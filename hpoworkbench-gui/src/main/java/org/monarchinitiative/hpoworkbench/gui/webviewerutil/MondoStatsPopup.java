package org.monarchinitiative.hpoworkbench.gui.webviewerutil;

import javafx.stage.Stage;
import org.monarchinitiative.hpoworkbench.analysis.MondoStats;
import org.monarchinitiative.hpoworkbench.html.MondoStatsHtmlGenerator;

public class MondoStatsPopup extends WebViewerPopup {
    private final String html;
    public MondoStatsPopup(MondoStats stats, Stage stage) {
        super(stage);
        this.html = MondoStatsHtmlGenerator.getHTML(stats);
    }

    @Override
    public void popup() {
        showHtmlContent("Mondo Stats", html);
    }
}
