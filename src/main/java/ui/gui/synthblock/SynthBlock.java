package ui.gui.synthblock;

import database.Database;
import database.NoSuchPatchException;
import database.NoSuchSynthException;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import structscript.Interpreter;
import structscript.StructScriptException;
import structscript.polyphony.PolyphonyType;
import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.DC;
import synthesizer.sources.utils.KnobSource;
import synthesizer.sources.utils.Socket;
import synthesizer.sources.utils.SourceValue;
import ui.gui.draggable.Deletable;
import ui.gui.knob.Knob;
import ui.gui.sequencer.ControlButton;
import ui.synthcontrollers.SimpleSynthController;
import ui.synthcontrollers.SynthController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static database.Database.*;
import static ui.gui.draggable.DraggablesUtils.makeDraggable;
import static ui.gui.volume.VolumeUtils.makeVolumeSlider;

public class SynthBlock extends TitledPane implements Deletable {

    private static final double cellSize = 40;

    final String synth;
    final PolyphonyType polyphony;
    final Slider volumeSlider;
    final ComboBox<String> patchesList;
    final Interpreter interpreter;
    final Map<String, DoubleProperty> properties;
    final KnobsSourceValuesHandler handler;
    final Socket sound;
    final SynthBlockController synthBlockController;
    final SynthController synthController;
    final Label label;
    final ObservableList<String> patches = FXCollections.observableArrayList();

    public SynthBlock(String synth, PolyphonyType polyphony) throws NoSuchSynthException, StructScriptException {
        synthBlockController = new SynthBlockController();

        this.synth = synth;
        this.polyphony = polyphony;
        handler = new KnobsSourceValuesHandler();
        interpreter = new Interpreter(polyphony, handler);
        interpreter.run(getSynthStructure(synth));
        List<KnobSource> knobs = handler.getKnobs();
        int width, height;
        if (!knobs.isEmpty()) {
            width = knobs.stream().mapToInt(knob -> knob.getX() + knob.getSize()).max().getAsInt();
            //noinspection OptionalGetWithoutIsPresent
            height = knobs.stream().mapToInt(knob -> knob.getY() + knob.getSize()).max().getAsInt();
        } else {
            width = 0;
            height = 0;
        }
        GridPane gridPane = new GridPane();
        for (int i = 0; i < width; ++i) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / width);
            gridPane.getColumnConstraints().add(col);
        }
        for (int j = 0; j < height; ++j) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / height);
            gridPane.getRowConstraints().add(row);
        }
        gridPane.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        gridPane.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        gridPane.setPrefSize(width * cellSize, height * cellSize);
        properties = new HashMap<>();
        for (SourceValue sourceValue : handler.getValues()) {
            DoubleProperty property = new SimpleDoubleProperty();
            property.addListener((observable, oldValue, newValue) -> sourceValue.setValue(newValue.doubleValue()));
            property.setValue(sourceValue.getValue());
            properties.put(sourceValue.getName(), property);
        }
        for (KnobSource knobSource : knobs) {
            Knob knob = new Knob();
            knob.valueProperty().bindBidirectional(properties.get(knobSource.getName()));
            knob.setText(knobSource.getDescription());
            knob.setValue(knobSource.getValue());
            gridPane.add(knob, knobSource.getX(), knobSource.getY(), knobSource.getSize(), knobSource.getSize());
        }
        SourceValue volume = new SourceValue("volume");
        volumeSlider = new Slider(0, 1, 0);
        makeVolumeSlider(volumeSlider, volume);
        volumeSlider.setMinWidth(USE_PREF_SIZE);
        volumeSlider.setPrefWidth(cellSize * 2);
        volumeSlider.setMaxWidth(USE_PREF_SIZE);

        sound = new Socket(interpreter.getVoiceDistributor().attenuate(volume));
        synthController = new SimpleSynthController(interpreter.getVoiceDistributor());

        label = new Label(synth + " (" + polyphony.getShortName() + ")");
        label.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(label, Priority.ALWAYS);

        HBox titleBox = new HBox(label, volumeSlider);
        titleBox.setMaxWidth(Double.MAX_VALUE);
        titleBox.setPadding(new Insets(0, 10, 0, 0));
        titleBox.minWidthProperty().bind(this.widthProperty());

        this.setGraphic(titleBox);

        HBox controlPanel = new HBox();
        controlPanel.getStyleClass().addAll("control-block", "control-panel");
        {
            {
                Button saveButton = new ControlButton("Save");
                TextField patchName = new TextField();
                patchName.setPromptText("patch name");

                saveButton.setOnAction(e -> {
                    String patch = patchName.getText();
                    patchName.setText("");
                    savePatch(patch);
                    if(!patches.contains(patch))
                        patches.add(patch);
                });

                controlPanel.getChildren().addAll(saveButton, patchName);
            }

            {
                Region space = new Region();
                HBox.setHgrow(space, Priority.ALWAYS);
                controlPanel.getChildren().add(space);
            }

            {
                patchesList = new ComboBox<>();
                patchesList.setPromptText("patch");
                patchesList.setItems(patches);
                patches.addAll(getPatches(synth));

                Button loadButton = new ControlButton("Load");
                loadButton.setOnAction(e -> {
                    String patch = patchesList.getValue();
                    if (patch == null)
                        return;
                    loadPatch(patch);
                });
                controlPanel.getChildren().addAll(patchesList, loadButton);
            }
        }
        VBox box = new VBox(controlPanel, gridPane);
        box.setAlignment(Pos.CENTER);
        gridPane.getStyleClass().addAll("control-block");
        this.setContent(box);
        this.setMaxWidth(USE_PREF_SIZE);

        makeDraggable(this, label);
        synthBlockController.initialize();
    }

    public String getSynthName(){
        return synth;
    }

    public PolyphonyType getPolyphony(){
        return polyphony;
    }

    public void loadPatch(String patch){
        try {
            Map<String, Double> patchProperties = getPatch(synth, patch);
            for (Map.Entry<String, Double> property : patchProperties.entrySet())
                if (properties.containsKey(property.getKey()))
                    properties.get(property.getKey()).setValue(property.getValue());
        } catch (NoSuchPatchException e){
            throw new RuntimeException(e);
        }
    }

    public void savePatch(String patch){
        try {
            Map<String, Double> patchProperties = new HashMap<>();
            for(Map.Entry<String, DoubleProperty> property : properties.entrySet())
                patchProperties.put(property.getKey(), property.getValue().doubleValue());
            Database.savePatch(synth, patch, patchProperties);
        } catch (NoSuchSynthException e) {
            throw new RuntimeException(e);
        }
    }

    public double getVolume() {
        return volumeSlider.getValue();
    }

    public void setVolume(double volume) {
        volumeSlider.setValue(volume);
    }

    public String getChosenPatch(){
        return patchesList.getValue();
    }

    public SynthController getSynthController() {
        return synthController;
    }

    public SignalSource getSound() {
        return sound;
    }

    public void setLabelContextMenu(ContextMenu contextMenu){
        label.setContextMenu(contextMenu);
    }

    @Override
    public void onDelete() {
        sound.bind(new DC(0));
    }

}
