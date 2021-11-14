package org.monarchinitiative.hpoworkbench.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class PasswordPopup {
    private static final Logger logger = LoggerFactory.getLogger(PasswordPopup.class);
    private String uname=null;
    private String pword=null;



    public PasswordPopup() {
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

        root.getChildren().addAll(new Label("Enter GitHub user name and password"),
                new Label("Data will be stored encrypted on disc."),
                new Label("Enter only github username if desired"));



        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> window.close());
        Button okButton = new Button("Store Username/Password");

        GridPane grid=new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));


        Label userName = new Label("GitHub Username:");
        grid.add(userName, 0, 0);

        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 0);

        Label pw = new Label("GitHub Password:");
        grid.add(pw, 0, 1);
        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 1);
        okButton.setOnAction(e -> {
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

    public String getUsername(){ return uname;}
    public String getPassword() { return pword;}



}
