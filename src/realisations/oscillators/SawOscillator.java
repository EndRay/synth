package realisations.oscillators;

import sources.SignalSource;
import sources.oscillators.AbstractOscillator;
import sources.utils.Socket;
import sources.utils.SourceValue;

/**
 * ramp-down saw
 */

public class SawOscillator extends AbstractOscillator {
    Socket hardSync = new Socket();
    private boolean lastGate = false;
    double lastSample = 0;

    public SawOscillator() {
    }

    public SawOscillator(double frequency) {
        super(frequency);
    }

    public SawOscillator(SignalSource frequencySource) {
        super(frequencySource);
    }

    public SawOscillator(SignalSource frequencySource, boolean randomPhase) {
        super(frequencySource, randomPhase);
    }

    public Socket hardSync(){
        return hardSync;
    }

    @Override
    public double getSample(int sampleId) {
        if(checkAndUpdateSampleId(sampleId)) {
            boolean g = hardSync().getGate(sampleId);
            if(!lastGate && g)
                setPtr(0);
            lastGate = g;
            lastSample = ((0.5 - getPtr(sampleId)) * 2);
        }
        return lastSample;
    }
}
