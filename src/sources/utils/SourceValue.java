package sources.utils;

import sources.AbstractSignalSource;
import sources.SignalSource;

import static utils.FrequencyManipulations.*;

public class SourceValue extends AbstractSignalSource implements SignalSource {

    double value;
    private String description;

    public SourceValue(){
        this("", 0);
    }
    public SourceValue(String description){
        this(description, 0);
    }
    public SourceValue(double initialValue){
        this("", initialValue);
    }

    public SourceValue(String description, double initialValue){
        this.description = description;
        value = initialValue;
    }

    public void setValue(double value){
        this.value = value;
    }
    public void setFrequency(double frequency){
        setValue(SignalSource.frequencyToVoltage(frequency));
    }
    public void setNote(double note){
        setFrequency(getFrequencyBySemitones(note));
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    @Override
    public double getSample(int sampleId) {
        return value;
    }
}
