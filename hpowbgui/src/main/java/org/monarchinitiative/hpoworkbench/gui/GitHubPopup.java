package org.monarchinitiative.hpoworkbench.gui;

import com.github.phenomics.ontolib.formats.hpo.HpoTerm;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    private String uname=null;
    private String pword=null;

    String githubIssueText=null;

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

        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(5);

        root.getChildren().add(new Label(String.format("Enter new GitHub issue about %s:",termlabel)));

        TextArea textArea = new TextArea();
        textArea.setText(getInitialText() );
        root.getChildren().add(textArea);

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> window.close());
        Button okButton = new Button("Create GitHub issue");

        GridPane grid=new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
//        Text scenetitle = new Text("Welcome");
//        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
//        grid.add(scenetitle, 0, 0, 2, 1);

        Label userName = new Label("GitHub Username:");
        grid.add(userName, 0, 0);

        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 0);

        Label pw = new Label("GitHub Password:");
        grid.add(pw, 0, 1);
        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 1);
        okButton.setOnAction(e -> {
            githubIssueText=textArea.getText();
            uname=userTextField.getText();
            pword=pwBox.getText();
            window.close();
        });
        HBox hbox= new HBox();
        hbox.setSpacing(10);
        hbox.getChildren().addAll(cancelButton,okButton);

        root.getChildren().add(hbox);
        root.getChildren().add(grid);
        Scene scene = new Scene(root, 500, 400);

        window.setScene(scene);
        window.showAndWait();
    }


    private String getInitialText() {
        return String.format("Suggestion about term %s [%s]\nCurrent definition: %s\n" +
                        "Current comment: %s\nCurrent synonym list: %s\n\n" +
                        "My suggestion: \n",
               termlabel,
                termid,
                definition!=null?definition:"no definition available",
                comment!=null?comment:"no comment available",
                synlist!=null?synlist:"-");
    }


    public String retrieveGitHubIssue(){ return githubIssueText; }
    public String getGitHubUserName() { return uname;}
    public String getGitHubPassWord() { return pword;}



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
