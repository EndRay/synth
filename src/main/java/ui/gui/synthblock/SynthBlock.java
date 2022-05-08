package ui.gui.synthblock;

import database.NoSuchPatchException;
import database.NoSuchSynthException;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import structscript.Interpreter;
import structscript.StructScriptException;
import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.KnobSource;
import synthesizer.sources.utils.SourceValue;
import ui.gui.knob.Knob;
import ui.synthcontrollers.SimpleSynthController;
import ui.synthcontrollers.SynthController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static database.Database.getPatch;
import static database.Database.getSynthStructure;

public class SynthBlock extends TitledPane {

    private static final double cellSize = 40;

    final String synth;
    final Interpreter interpreter;
    final Map<String, DoubleProperty> properties;
    final KnobsSourceValuesHandler handler;
    final SignalSource sound;
    final SynthBlockController synthBlockController;
    final SynthController synthController;
    final Label label;

    public SynthBlock(String synth, int voiceCount) throws NoSuchSynthException, StructScriptException {
        this.synth = synth;
        handler = new KnobsSourceValuesHandler();
        interpreter = new Interpreter(voiceCount, handler);
        interpreter.run(getSynthStructure(synth));
        List<KnobSource> knobs = handler.getKnobs();
        if (knobs.isEmpty())
            throw new RuntimeException("synth doesn't have knobs");
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        int width = knobs.stream().mapToInt(knob -> knob.getX() + knob.getSize()).max().getAsInt(),
                height = knobs.stream().mapToInt(knob -> knob.getY() + knob.getSize()).max().getAsInt();
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
        Slider volumeSlider = new Slider(0, 1, 0);
        volumeSlider.setOrientation(Orientation.VERTICAL);
        volumeSlider.setPrefHeight(cellSize * 3);
        volumeSlider.setMinHeight(cellSize);
        volumeSlider.setMaxHeight(USE_PREF_SIZE);
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> volume.setValue(newValue.doubleValue()));

        sound = interpreter.getVoiceDistributor().attenuate(volume);
        synthController = new SimpleSynthController(interpreter.getVoiceDistributor());

        label = new Label(synth + " (" + voiceCount + ")");
        label.minWidthProperty().bind(this.widthProperty());

        this.setGraphic(label);
        this.setContent(new HBox(gridPane, volumeSlider));
        this.setMaxWidth(USE_PREF_SIZE);

        synthBlockController = new SynthBlockController();
        synthBlockController.pane = this;
        synthBlockController.initialize();
        label.setOnMousePressed(synthBlockController::onPressed);
        label.setOnMouseDragged(synthBlockController::onDragged);
        label.setOnMouseReleased(synthBlockController::onReleased);
    }

    public void loadPatch(String patch) throws NoSuchPatchException {
        Map<String, Double> patchProperties = getPatch(synth, patch);
        for (Map.Entry<String, Double> property : patchProperties.entrySet())
            if (properties.containsKey(property.getKey()))
                properties.get(property.getKey()).setValue(property.getValue());
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
}
