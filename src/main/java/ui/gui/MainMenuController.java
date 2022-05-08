package ui.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import synthesizer.sources.utils.DC;

import java.io.IOException;

import static ui.gui.MainGUI.sound;

public class MainMenuController {

    @FXML void goToEdit(ActionEvent event){
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        try {
            Parent root = new FXMLLoader(EditController.class.getResource("main-editor.fxml")).load();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML void goToPlay(ActionEvent event){
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        try {
            Parent root = new FXMLLoader(EditController.class.getResource("playground.fxml")).load();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML void initialize(){
        sound.bind(new DC(0));
    }
}
