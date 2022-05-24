package ui.gui.sequencer;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyStringProperty;
import sequencer.MeasureDivision;

public interface MeasureDivisionProperty extends Property<MeasureDivision> {
    ReadOnlyStringProperty nameProperty();
}
