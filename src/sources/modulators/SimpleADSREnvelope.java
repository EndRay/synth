package sources.modulators;

import sources.AbstractSignalSource;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class SimpleADSREnvelope extends AbstractSignalSource implements Envelope {
    double attack, decay, sustain, release;
    double currentSample;
    boolean legato, retrigger;
    boolean gate, attackStage;

    public SimpleADSREnvelope(double release) {
        this(0, release, 0, release, false, false);
    }

    public SimpleADSREnvelope(double attack, double release) {
        this(attack, release, 0, release, false, false);
    }

    public SimpleADSREnvelope(double attack, double decay, double release) {
        this(attack, decay, 0, release, false, false);
    }

    public SimpleADSREnvelope(double attack, double decay, double sustain, double release) {
        this(attack, decay, sustain, release, false, false);
    }

    public SimpleADSREnvelope(double attack, double decay, double sustain, double release, boolean legato, boolean retrigger) {
        this.attack = attack;
        this.decay = decay;
        this.sustain = sustain;
        this.release = release;
        this.legato = legato;
        this.retrigger = retrigger;
        currentSample = 0;
        gate = false;
        attackStage = false;
    }

    public double getSample(int sampleId) {
        if (checkAndUpdateSampleId(sampleId)) {
            if (attackStage) {
                currentSample += 1 / attack / sampleRate;
                if (currentSample >= 1) {
                    currentSample = 1;
                    attackStage = false;
                }
            }
            else if (!gate)
                currentSample = max(currentSample - 1 / release / sampleRate, 0);
            else currentSample = max(currentSample - 1 / decay / sampleRate, sustain);
        }
        return currentSample;
    }

    @Override
    public void gateOn() {
        if (!legato || !gate) {
            attackStage = true;
            if(retrigger)
                currentSample = 0;
        }
        gate = true;
    }

    public void gateOff() {
        attackStage = false;
        gate = false;
    }
}
