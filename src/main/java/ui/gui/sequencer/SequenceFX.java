package ui.gui.sequencer;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import sequencer.MeasureDivision;
import sequencer.Sequence;

public class SequenceFX{

    private Sequence sequence;
    private final IntegerProperty stepNumberProperty = new SimpleIntegerProperty(0);
    private final MeasureDivisionProperty measureDivisionProperty = new SimpleMeasureDivisionProperty();

    public SequenceFX(){
        this(null);
    }

    void setSequenceMeasureDivision(MeasureDivision measureDivision){
        sequence.setMeasureDivision(measureDivision);
    }
    public SequenceFX(Sequence sequence) {
        this.sequence = sequence;
        measureDivisionProperty.addListener((observable, oldValue, newValue) -> setSequenceMeasureDivision(newValue));
    }

    public void setSequence(Sequence sequence){
        this.sequence = sequence;
    }

    public void updateProperties(){
        if(sequence == null){
            stepNumberProperty.set(0);
            measureDivisionProperty.setValue(null);
        }
        stepNumberProperty.set(sequence.length());
        measureDivisionProperty.setValue(sequence.getMeasureDivision());
    }

    public MeasureDivisionProperty measureDivisionProperty(){
        return measureDivisionProperty;
    }

    public ReadOnlyIntegerProperty stepNumberProperty() {
        return stepNumberProperty;
    }
}
