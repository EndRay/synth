package ui.gui;

import database.NoSuchSynthException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import structscript.StructScriptException;
import synthesizer.sources.utils.Socket;
import ui.gui.synthblock.SynthBlock;

import java.io.IOException;

import static midi.SynthMidiReceiver.channels;
import static ui.gui.MainGUI.*;

public class PlayController {

    Socket playgroundSound = new Socket();

    @FXML Pane table;
    @FXML TextField synthNameField;
    @FXML TextField voiceCountField;

    @FXML TextField messageText;

    @FXML Slider masterVolumeSlider;

    @FXML void goToMainMenu(ActionEvent event){
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        try {
            Parent root = new FXMLLoader(EditController.class.getResource("main-menu.fxml")).load();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML void createSynthBlock(){
        String synth = synthNameField.getText();
        try {
            int voiceCount = Integer.parseInt(voiceCountField.getCharacters().toString());
            SynthBlock synthBlock = new SynthBlock(synth, voiceCount);
            table.getChildren().add(synthBlock);
            playgroundSound.modulate(synthBlock.getSound());

            ContextMenu menu = new ContextMenu();
            for(int i = 0; i < channels; ++i){
                int channel = i;
                CheckMenuItem item = new CheckMenuItem("midi channel " + (channel+1));
                item.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if(newValue)
                        receiver.addSynthController(channel, synthBlock.getSynthController());
                    else receiver.removeSynthController(channel, synthBlock.getSynthController());
                });
                menu.getItems().add(item);
            }
            synthBlock.setLabelContextMenu(menu);

            messageText.setText("synth created successfully");
        } catch (NoSuchSynthException e) {
            messageText.setText("no such synth \"" + synth + "\"");
        } catch (StructScriptException e) {
            messageText.setText(e.getStructScriptMessage());
        } catch (NumberFormatException e) {
            messageText.setText("voice count must be an integer");
        }
    }

    @FXML void initialize(){
        sound.bind(playgroundSound);
        masterVolumeSlider.setValue(masterVolume.getValue());
        masterVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> masterVolume.setValue(newValue.doubleValue()));
    }
}
