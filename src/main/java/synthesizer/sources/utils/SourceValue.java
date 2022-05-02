package synthesizer.sources.utils;

import synthesizer.sources.AbstractSignalSource;
import synthesizer.sources.SignalSource;

import static synthesizer.utils.FrequencyManipulations.*;

public class SourceValue extends AbstractSignalSource implements SignalSource {

    double value;
    private String name;

    public SourceValue(){
        this("", 0);
    }


    public SourceValue(String name){
        this(name, 0);
    }
    public SourceValue(double initialValue){
        this("", initialValue);
    }

    public SourceValue(String name, double initialValue){
        this.name = name;
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

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    @Override
    protected double recalculate(int sampleId) {
        return value;
    }
}
