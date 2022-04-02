package sources.utils;

import sources.AbstractSignalSource;

public class Triggerable extends SourceValue {

    private double lastGate = 0;

    public Triggerable(){

    }
    public Triggerable(String description){
        super(description);
    }
    public Triggerable(double initialValue){
        super(initialValue);
    }

    public Triggerable(String description, double initialValue){
        super(description, initialValue);
    }

    public void setValue(double value){
        this.lastGate = value;
    }

    @Override
    public double getSample(int sampleId) {
        if(checkAndUpdateSampleId(sampleId)){
            value = lastGate;
            lastGate = 0;
        }
        return value;
    }



    public void trigger(){
        setValue(1);
    }
}
