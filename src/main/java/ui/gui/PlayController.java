package ui.gui;

import database.NoSuchSynthException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import structscript.StructScriptException;
import structscript.polyphony.PolyphonyException;
import structscript.polyphony.PolyphonyType;
import structscript.polyphony.PolyphonyUtils;
import synthesizer.sources.utils.Socket;
import ui.gui.keyboardblock.KeyboardBlock;
import ui.gui.synthblock.SynthBlock;

import java.io.IOException;
import java.security.Key;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
            PolyphonyType polyphony = PolyphonyUtils.byString(voiceCountField.getCharacters().toString());
            SynthBlock synthBlock = new SynthBlock(synth, polyphony);
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

            messageText.setText("synth successfully created");
        } catch (NoSuchSynthException e) {
            messageText.setText("no such synth \"" + synth + "\"");
        } catch (StructScriptException e) {
            messageText.setText(e.getStructScriptMessage());
        } catch (PolyphonyException e) {
            messageText.setText("incorrect polyphony type");
        }
    }

    Set<KeyCode> pressedKeys = new HashSet<>();
    KeyConsumer focusedConsumer;
    ReadWriteLock focusedConsumerLock = new ReentrantReadWriteLock();

    void setFocusedConsumer(KeyConsumer consumer){
        if(focusedConsumer == consumer)
            return;
        try{
            focusedConsumerLock.writeLock().lock();
            if (focusedConsumer != null)
                focusedConsumer.unfocus();
            focusedConsumer = consumer;
        } finally {
            focusedConsumerLock.writeLock().unlock();
        }
    }

    KeyConsumer getFocusedConsumer(){
        try {
            focusedConsumerLock.readLock().lock();
            return focusedConsumer;
        } finally {
            focusedConsumerLock.readLock().unlock();
        }
    }

    void configureSceneKeyConsuming(){
        Scene scene = table.getScene();
        scene.focusOwnerProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue instanceof KeyConsumer consumer)
                setFocusedConsumer(consumer);
        });
        scene.setOnKeyPressed(event -> {
            if(pressedKeys.contains(event.getCode()))
                return;
            pressedKeys.add(event.getCode());
            KeyConsumer consumer = getFocusedConsumer();
            if(consumer != null)
                consumer.keyPressConsume(event.getCode());
        });
        scene.setOnKeyReleased(event -> {
            if(!pressedKeys.contains(event.getCode()))
                return;
            pressedKeys.remove(event.getCode());
            KeyConsumer consumer = getFocusedConsumer();
            if(consumer != null)
                consumer.keyReleaseConsume(event.getCode());
        });
    }

    @FXML void createKeyboardBlock(){
        configureSceneKeyConsuming();

        KeyboardBlock keyboardBlock = new KeyboardBlock(receiver);
        table.getChildren().add(keyboardBlock);

        ToggleGroup group = new ToggleGroup();

        ContextMenu menu = new ContextMenu();
        for(int i = 0; i < channels; ++i){
            int channel = i;
            RadioMenuItem item = new RadioMenuItem("midi channel " + (channel+1));
            item.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue)
                    keyboardBlock.setChannel(channel);
            });
            item.setToggleGroup(group);
            menu.getItems().add(item);
        }
        {
            RadioMenuItem item = new RadioMenuItem("disabled");
            item.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue)
                    keyboardBlock.setChannel(-1);
            });
            menu.getItems().add(item);
            item.setToggleGroup(group);
            item.setSelected(true);
        }
        keyboardBlock.setLabelContextMenu(menu);

        messageText.setText("keyboard successfully created");
    }

    @FXML void initialize(){
        sound.bind(playgroundSound);
        masterVolumeSlider.setValue(masterVolume.getValue());
        masterVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> masterVolume.setValue(newValue.doubleValue()));
    }
}
