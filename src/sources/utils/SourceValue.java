package sources.utils;

import sources.SignalSource;

public class SourceValue implements SignalSource {

    double value;
    String description;

    public SourceValue(){
        this("", 0);
    }
    public SourceValue(String description){
        this(description, 0);
    }
    public SourceValue(double initialValue){
        this("", 0);
    }

    public SourceValue(String description, double initialValue){
        this.description = description;
        value = initialValue;
    }

    public void setValue(double value){
        this.value = value;
    }

    @Override
    public double getSample(int sampleId) {
        return value;
    }
}
