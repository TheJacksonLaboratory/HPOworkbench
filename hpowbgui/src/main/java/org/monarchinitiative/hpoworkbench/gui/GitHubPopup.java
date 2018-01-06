package org.monarchinitiative.hpoworkbench.gui;

import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

public class GitHubPopup {
    private static final Logger logger = LogManager.getLogger();

    private final String termlabel;
    private final String termid;
    private final String definition;
    private final String comment;
    private final String synlist;

    public GitHubPopup(HpoTerm term) {
        termlabel=term.getName();
        termid=term.getId().getIdWithPrefix();
        definition=term.getDefinition();
        comment=term.getComment();
        synlist=term.getSynonyms().size()==0?"-":term.getSynonyms().
                stream().map(s->s.getValue()).collect(Collectors.joining(";"));
    }

    public void displayWindow(Stage ownerWindow) {
        Stage window = new Stage();
        window.setResizable(false);
        window.centerOnScreen();
        window.setTitle("New github issue");
        window.initStyle(StageStyle.UTILITY);
        window.initModality(Modality.APPLICATION_MODAL);



//        Stage adjWindow = adjustStagePosition(window, ownerWindow);
//        adjWindow.initStyle(StageStyle.DECORATED);
//        adjWindow.setResizable(true);

        WebView browser = new WebView();
        WebEngine engine = browser.getEngine();
        engine.load(getHTML());

//        adjWindow.setScene(new Scene(browser));
//        adjWindow.showAndWait();
        window.setScene(new Scene(browser));
        window.showAndWait();


    }


    private String getHTML() {
        final String HTML_TEMPLATE = String.format("<!DOCTYPE html>" +
                "<html lang=\"en\"><head>" +
               // "<style>%s</style>\n" +
                "<meta charset=\"UTF-8\"><title>Current term</title></head>" +
                "<body>" +
                "<h1>%s</h1>" +
                "<p><b>ID:</b> %s</p>" +
                "<p><b>Definition:</b> %s</p>" +
                "<p><b>Comment:</b> %s</p>" +
                "<p><b>Synonyms:</b> %s</p>" +
                "</body></html>",termlabel,termid,definition,comment,synlist);
        logger.trace(HTML_TEMPLATE);
        return HTML_TEMPLATE;
    }



    /**
     * Ensure that popup Stage will be displayed on the same monitor as the parent Stage
     *
     * @param childStage
     * @param parentStage
     * @return
     */
    private static Stage adjustStagePosition(Stage childStage, Stage parentStage) {
        ObservableList<Screen> screensForParentWindow = Screen.getScreensForRectangle(parentStage.getX(), parentStage.getY(),
                parentStage.getWidth(), parentStage.getHeight());
        Screen actual = screensForParentWindow.get(0);
        Rectangle2D bounds = actual.getVisualBounds();

        // set top left position to 35%/25% of screen/monitor width & height
        childStage.setX(bounds.getWidth() * 0.35);
        childStage.setY(bounds.getHeight() * 0.25);
        return childStage;
    }

}
