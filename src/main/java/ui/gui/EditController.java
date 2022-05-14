package ui.gui;

import database.NoSuchPatchException;
import database.NoSuchSynthException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import structscript.Interpreter;
import structscript.StructScriptException;
import structscript.polyphony.PolyphonyException;
import structscript.polyphony.PolyphonyType;
import structscript.polyphony.PolyphonyUtils;
import synthesizer.VoiceDistributor;
import synthesizer.sources.utils.Socket;
import ui.synthcontrollers.SimpleSynthController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static database.Database.*;
import static ui.gui.MainGUI.*;

public class EditController {

    Socket editorSound = new Socket();

    PropertiesSourceValuesHandler sourceValuesHandler = null;

    ObservableList<Region> list;

    KeyCombination buildHotkey = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);

    @FXML TextArea structureField;
    @FXML TextField synthNameField;
    @FXML TextField voiceCountField;
    @FXML Button buildButton;

    @FXML TextField leftPatchNameField;
    @FXML TextField rightPatchNameField;

    @FXML ListView<Region> propertiesList;

    @FXML Slider morphSlider;

    @FXML Slider masterVolumeSlider;

    @FXML TextField messageText;

    ObservableList<Value> values;
    BooleanProperty leftPatchEditing = new SimpleBooleanProperty(true);
    BooleanProperty rightPatchEditing = new SimpleBooleanProperty(false);
    Map<String, Double> leftPatch = new HashMap<>(), rightPatch = new HashMap<>();

    private Map<String, Double> morphPatch(double morph){
        Map<String, Double> res = new HashMap<>();
        for(Value value : values)
            if(value.value() != null)
                res.put(value.name(), leftPatch.get(value.name()) * (1 - morph) + rightPatch.get(value.name()) * morph);
        return res;
    }
    private void assignMorphValues(double morph){
        Map<String, Double> patch = morphPatch(morph);
        for(Value value : values)
            if(value.value() != null && patch.containsKey(value.name()))
                value.value().set(patch.get(value.name()));
    }

    @FXML void goToMainMenu(ActionEvent event){
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        try {
            Parent root = new FXMLLoader(EditController.class.getResource("main-menu.fxml")).load();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML void onSynthLoadButtonClick(){
        String name = synthNameField.getCharacters().toString().trim();
        try {
            structureField.setText(getSynthStructure(name));
            messageText.setText("synth \"" + name + "\" loaded successfully");
        } catch (NoSuchSynthException e) {
            messageText.setText("no such synth \"" + name + "\"");
        }
    }
    @FXML void onSynthSaveButtonClick(){
        String name = synthNameField.getCharacters().toString().trim();
        String code = structureField.getText();
        saveSynth(name, code);
        messageText.setText("synth \"" + name + "\" saved successfully");
    }
    @FXML void onSynthBuildButtonClick(){
        String structure = structureField.getText();
        try {
            PolyphonyType polyphony = PolyphonyUtils.byString(voiceCountField.getCharacters().toString().trim());
            sourceValuesHandler.resetValues();
            Interpreter interpreter = new Interpreter(polyphony, sourceValuesHandler);
            interpreter.run(structure);
            VoiceDistributor distributor = interpreter.getVoiceDistributor();
            editorSound.bind(distributor);
            receiver.clearSynthControllers(0);
            receiver.addSynthController(0, new SimpleSynthController(distributor));
            messageText.setText("synth built successfully");
        } catch (PolyphonyException e) {
            messageText.setText("incorrect polyphony type");
        } catch (StructScriptException e) {
            messageText.setText(e.getStructScriptMessage());
            int linePos = 0;
            for (int i = 0; i < e.getLine() - 1; ++i)
                linePos = structure.indexOf('\n', linePos) + 1;
            int linePosTo = structure.indexOf('\n', linePos);
            structureField.positionCaret(linePos);
            if (linePosTo != -1)
                structureField.selectPositionCaret(linePosTo);
            else structureField.selectEnd();
        }
    }

    @FXML void onLeftPatchLoadButtonClick(){
        String synth = synthNameField.getText();
        String patch = leftPatchNameField.getText();
        try {
            Map<String, Double> newPatch = getPatch(synth, patch);
            leftPatch.putAll(newPatch);
            assignMorphValues(morphSlider.getValue());
            messageText.setText("synth \"" + synth + "\" patch \"" + patch + "\" loaded successfully");
        } catch (NoSuchPatchException e) {
            messageText.setText("synth \"" + synth + "\" has no such patch \"" + patch + "\"");
        }
    }
    @FXML void onLeftPatchSaveButtonClick(){
        String synth = synthNameField.getText();
        String patch = leftPatchNameField.getText();
        try {
            Map<String, Double> parameters = new HashMap<>();
            for(Value value : values)
                if(value.value() != null)
                    parameters.put(value.name(), value.value().getValue());
            savePatch(synth, patch, parameters);
            messageText.setText("synth \"" + synth + "\" patch \"" + patch + "\" saved successfully");
        } catch (NoSuchSynthException e) {
            messageText.setText("no such synth \"" + synth + "\"");
        }
    }

    @FXML void onRightPatchLoadButtonClick(){
        String synth = synthNameField.getText();
        String patch = rightPatchNameField.getText();
        try {
            Map<String, Double> newPatch = getPatch(synth, patch);
            rightPatch.putAll(newPatch);
            assignMorphValues(morphSlider.getValue());
            messageText.setText("synth \"" + synth + "\" patch \"" + patch + "\" loaded successfully");
        } catch (NoSuchPatchException e) {
            messageText.setText("synth \"" + synth + "\" has no such patch \"" + patch + "\"");
        }
    }
    @FXML void onRightPatchSaveButtonClick(){
        String synth = synthNameField.getText();
        String patch = rightPatchNameField.getText();
        try {
            Map<String, Double> parameters = new HashMap<>();
            for(Value value : values)
                if(value.value() != null)
                    parameters.put(value.name(), value.value().getValue());
            savePatch(synth, patch, parameters);
            messageText.setText("synth \"" + synth + "\" patch \"" + patch + "\" saved successfully");
        } catch (NoSuchSynthException e) {
            messageText.setText("no such synth \"" + synth + "\"");
        }
    }

    public void initialize(){
        sound.bind(editorSound);
        masterVolumeSlider.setValue(masterVolume.getValue());
        masterVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> masterVolume.setValue(newValue.doubleValue()));

        structureField.setOnKeyPressed(event -> {
            if(buildHotkey.match(event))
                buildButton.fire();
        });

        sourceValuesHandler = new PropertiesSourceValuesHandler();
        values = sourceValuesHandler.getValues();

        list = FXCollections.observableArrayList();
        propertiesList.setItems(list);
        values.addListener((ListChangeListener<Value>) change -> {
            list.clear();
            for (Value value : change.getList()) {
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER);
                if (value.value() == null) {
                    Text text = new Text(value.name());
                    text.setFont(Font.font("", FontWeight.BOLD, 20));

                    row.getChildren().addAll(text);
                } else {
                    value.value().addListener((observable, oldValue, newValue) -> {
                        if(leftPatchEditing.get())
                            leftPatch.put(value.name(), newValue.doubleValue());
                        if(rightPatchEditing.get())
                            rightPatch.put(value.name(), newValue.doubleValue());
                    });
                    if(!leftPatch.containsKey(value.name()))
                        leftPatch.put(value.name(), value.value().doubleValue());
                    if(!rightPatch.containsKey(value.name()))
                        rightPatch.put(value.name(), value.value().doubleValue());

                    Slider slider = new Slider();
                    slider.setMin(0);
                    slider.setMax(1);
                    slider.valueProperty().bindBidirectional(value.value());
                    slider.setMajorTickUnit(0.5);
                    slider.setMinorTickCount(5);
                    slider.setShowTickMarks(true);
                    slider.disableProperty().bind(leftPatchEditing.or(rightPatchEditing).not());
                    Text text = new Text(value.name());
                    text.setFont(Font.font(16));
                    VBox textBox = new VBox(text);
                    textBox.setPrefWidth(150);
                    textBox.setAlignment(Pos.CENTER);
                    HBox.setHgrow(slider, Priority.ALWAYS);
                    row.getChildren().addAll(slider, textBox);
                }
                list.add(row);
            }
            assignMorphValues(morphSlider.getValue());
        });

        morphSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            leftPatchEditing.set(newValue.doubleValue() == 0);
            rightPatchEditing.set(newValue.doubleValue() == 1);
            assignMorphValues(newValue.doubleValue());
        });
    }
}
