package ui.gui.sequencer;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import sequencer.MeasureDivision;
import sequencer.Sequence;
import sequencer.Step;

public class SequenceFX{

    private Sequence sequence;
    private final IntegerProperty stepNumberProperty = new SimpleIntegerProperty(0);
    private final MeasureDivisionProperty measureDivisionProperty = new SimpleMeasureDivisionProperty();

    public SequenceFX(Sequence sequence) {
        this.sequence = sequence;
        measureDivisionProperty.addListener((observable, oldValue, newValue) -> sequence.setMeasureDivision(newValue));
    }

    public void changeSequence(Sequence sequence){
        this.sequence = sequence;
    }

    public void updateProperties(){
        stepNumberProperty.set(sequence.length());
        measureDivisionProperty.setValue(sequence.getMeasureDivision());
    }

    public MeasureDivisionProperty measureDivisionProperty(){
        return measureDivisionProperty;
    }

    public ReadOnlyIntegerProperty stepNumber() {
        return stepNumberProperty;
    }
}
