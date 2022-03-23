package sources.oscillators;

import sources.SignalSource;
import sources.utils.DC;

abstract public class AbstractPMOscillator extends AbstractOscillator implements PMOscillator{

    SignalSource phaseSource;

    public AbstractPMOscillator(SignalSource frequencySource) {
        this(frequencySource, new DC(0));
    }

    public AbstractPMOscillator(SignalSource frequencySource, SignalSource phaseSource) {
        super(frequencySource);
        this.phaseSource = phaseSource;
    }

    public double getPtr(int sampleId){
        double ptr = super.getPtr(sampleId);
        ptr += getPhase(sampleId);
        while(ptr < 0)
            ptr += 1;
        while(ptr >= 1)
            ptr -= 1;
        return ptr;
    }

    @Override
    public double getPhase(int sampleId) {
        return phaseSource.getSample(sampleId);
    }

    @Override
    public void setPhase(SignalSource phaseSource) {
        this.phaseSource = phaseSource;
    }
}
