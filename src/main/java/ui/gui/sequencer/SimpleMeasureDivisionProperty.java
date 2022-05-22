package ui.gui.sequencer;

import javafx.beans.property.*;
import sequencer.MeasureDivision;

public class SimpleMeasureDivisionProperty extends SimpleObjectProperty<MeasureDivision> implements MeasureDivisionProperty{
    private final StringProperty nameProperty = new SimpleStringProperty();

    public SimpleMeasureDivisionProperty(){
        this(null);
    }

    public SimpleMeasureDivisionProperty(MeasureDivision measureDivision){
        super(measureDivision);
        this.addListener((observable, oldValue, newValue) -> nameProperty.set(newValue.getShortName()));
    }

    @Override
    public ReadOnlyStringProperty nameProperty(){
        return nameProperty;
    }
}
