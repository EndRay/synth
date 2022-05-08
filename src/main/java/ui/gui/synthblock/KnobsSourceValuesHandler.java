package ui.gui.synthblock;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import structscript.SourceValuesHandler;
import synthesizer.sources.utils.KnobSource;
import synthesizer.sources.utils.SourceValue;
import ui.gui.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnobsSourceValuesHandler implements SourceValuesHandler {

    Map<String, DoubleProperty> savedValues = new HashMap<>();

    List<SourceValue> sourceValues = new ArrayList<>();
    List<KnobSource> knobs = new ArrayList<>();

    @Override
    public void addSourceValue(SourceValue sourceValue) {
        sourceValues.add(sourceValue);
        if(sourceValue instanceof KnobSource knob)
            knobs.add(knob);
    }

    @Override
    public void addSection(String name) {

    }

    public List<KnobSource> getValues() {
        return knobs;
    }
    public List<KnobSource> getKnobs() {
        return knobs;
    }
}
