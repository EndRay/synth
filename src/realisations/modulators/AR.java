package realisations.modulators;

import sources.AbstractSignalSource;
import sources.SignalSource;
import sources.modulators.Envelope;
import sources.utils.DC;
import sources.utils.Socket;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class AR extends AbstractSignalSource implements Envelope {
    private final Socket attack, release;
    private double currentSample;
    //private boolean legato, retrigger;
    private boolean gate;


    public AR(double attack,double release) {
        this(new DC(attack), new DC(release));
    }
    public AR(SignalSource attackSource, SignalSource releaseSource) {
        attack = new Socket(attackSource);
        release = new Socket(releaseSource);
        currentSample = 0;
        gate = false;
    }

    public Socket attack(){
        return attack;
    }
    public Socket release(){
        return release;
    }

    public double getSample(int sampleId) {
        if (checkAndUpdateSampleId(sampleId)) {
            if (gate)
                currentSample += min(1 / attack().getTime(sampleId) / sampleRate, 1);
            else currentSample = max(currentSample - 1 / release().getTime(sampleId) / sampleRate, 0);
        }
        return currentSample;
    }

    @Override
    public void gateOn() {
        gate = true;
    }

    public void gateOff() {
        gate = false;
    }
}
