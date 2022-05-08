package synthesizer.realisations.modulators;

import synthesizer.sources.AbstractSignalSource;
import synthesizer.sources.SignalSource;
import synthesizer.sources.modulators.Envelope;
import synthesizer.sources.utils.DC;
import synthesizer.sources.utils.Socket;

import static java.lang.Math.max;

public class AD extends AbstractSignalSource implements Envelope {
    private final Socket attack, decay, gate = new Socket(), trigger = new Socket();
    private double currentSample;
    private boolean attackStage;
    private boolean lastGate = false, lastTrigger = false, triggered = false;


    public AD(double attack, double decay) {
        this(new DC(attack), new DC(decay));
    }

    public AD(SignalSource attackSource, SignalSource decaySource) {
        attack = new Socket(attackSource);
        decay = new Socket(decaySource);
        currentSample = 0;
        attackStage = false;
    }

    public Socket attack() {
        return attack;
    }

    public Socket decay() {
        return decay;
    }

    public Socket gate() {
        return gate;
    }
    public Socket trigger(){ return trigger; }

    @Override
    protected double recalculate(int sampleId) {
        boolean g = gate().getGate(sampleId), t = trigger().getGate(sampleId);
        double attack = attack().getTime(sampleId),
                decay = decay().getTime(sampleId);
        triggered |= (!lastTrigger && t);
        attackStage |= (!lastGate && g);
        attackStage &= g;
        attackStage |= triggered;
        lastGate = g;
        lastTrigger = t;
        if (attackStage) {
            currentSample += 1 / attack / sampleRate;
            if (currentSample >= 1) {
                currentSample = 1;
                attackStage = false;
                triggered = false;
            }
        } else currentSample = max(currentSample - 1 / decay / sampleRate, 0);
        return currentSample;
    }
}
