package ui.gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import synthesizer.sources.utils.SourceValue;
import ui.structscript.SourceValuesHandler;

import java.util.HashMap;
import java.util.Map;

public class PropertiesSourceValuesHandler implements SourceValuesHandler {

    Map<String, DoubleProperty> savedValues = new HashMap<>();

    ObservableList<Value> values = FXCollections.observableArrayList();
    @Override
    public void addSourceValue(SourceValue sourceValue) {
        String name = sourceValue.getName();
        if(savedValues.containsKey(name))
            sourceValue.setValue(savedValues.get(name).doubleValue());
        DoubleProperty property = new SimpleDoubleProperty(sourceValue.getValue());
        property.addListener((observable, oldValue, newValue) -> sourceValue.setValue(newValue.doubleValue()));
        savedValues.put(name, property);
        values.add(new Value(name, property));
    }

    @Override
    public void addSection(String name) {
        values.add(new Value(name, null));
    }

    public void clearSavedValues(){
        savedValues.clear();
    }

    public void resetValues(){
        values.clear();
    }

    public ObservableList<Value> getValues(){
        return values;
    }
}
