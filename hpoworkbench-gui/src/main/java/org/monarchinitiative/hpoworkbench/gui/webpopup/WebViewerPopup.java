package org.monarchinitiative.hpoworkbench.gui.webpopup;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public abstract class WebViewerPopup {
    /** Reference to stage of primary window.*/
    private final Stage primaryStage;

    public WebViewerPopup(Stage stage) {
        this.primaryStage = stage;
    }

    protected void showHtmlContent(String windowTitle, String html) {
        Stage window = getPopUpStage(windowTitle);
        Stage adjWindow = adjustStagePosition(window, this.primaryStage);
        adjWindow.initStyle(StageStyle.DECORATED);
        adjWindow.setResizable(true);

        WebView browser = new WebView();
        WebEngine engine = browser.getEngine();
        engine.loadContent(html);
        adjWindow.setScene(new Scene(browser));
        adjWindow.showAndWait();
    }


    private Stage getPopUpStage(String title) {
        Stage window = new Stage();
        window.setResizable(false);
        window.centerOnScreen();
        window.setTitle(title);
        window.initStyle(StageStyle.UTILITY);
        window.initModality(Modality.APPLICATION_MODAL);
        return window;
    }

    /**
     * Ensure that popup Stage will be displayed on the same monitor as the parent Stage
     */
    private Stage adjustStagePosition(Stage childStage, Stage parentStage) {
        ObservableList<Screen> screensForParentWindow = Screen.getScreensForRectangle(parentStage.getX(), parentStage.getY(),
                parentStage.getWidth(), parentStage.getHeight());
        Screen actual = screensForParentWindow.get(0);
        Rectangle2D bounds = actual.getVisualBounds();

        // set top left position to 35%/25% of screen/monitor width & height
        childStage.setX(bounds.getWidth() * 0.35);
        childStage.setY(bounds.getHeight() * 0.25);
        return childStage;
    }

    protected String inlineCSS() {
        return "<style>\n" +
                "  html { margin: 20; padding: 20; }" +
                "body { font: 75% georgia, sans-serif; line-height: 1.88889;color: #001f3f; margin: 0; padding: 0; }"+
                "p { margin-top: 0;text-align: justify;}"+
                "h3 {font-family: 'serif';font-size: 1.4em;font-style: normal;font-weight: bold;"+
                "letter-spacing: 1px; margin-bottom: 0; color: #001f3f;}"+
                "  </style>";
    }

    public abstract void popup();

}
