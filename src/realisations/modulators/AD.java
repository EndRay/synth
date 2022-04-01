package realisations.modulators;

import sources.AbstractSignalSource;
import sources.SignalSource;
import sources.modulators.Envelope;
import sources.utils.DC;
import sources.utils.Socket;

import static java.lang.Math.max;

public class AD extends AbstractSignalSource implements Envelope {
    private final Socket attack, decay;
    private double currentSample;
    private boolean attackStage;


    public AD(double attack, double decay) {
        this(new DC(attack), new DC(decay));
    }
    public AD(SignalSource attackSource, SignalSource decaySource) {
        attack = new Socket(attackSource);
        decay = new Socket(decaySource);
        currentSample = 0;
        attackStage = false;
    }

    public Socket attack(){
        return attack;
    }
    public Socket decay(){
        return decay;
    }
    public double getSample(int sampleId) {
        if (checkAndUpdateSampleId(sampleId)) {
            if (attackStage) {
                currentSample += 1 / attack().getTime(sampleId) / sampleRate;
                if (currentSample >= 1) {
                    currentSample = 1;
                    attackStage = false;
                }
            }
            else currentSample = max(currentSample - 1 / decay().getTime(sampleId) / sampleRate, 0);
        }
        return currentSample;
    }

    @Override
    public void gateOn() {
        attackStage = true;
    }

    public void gateOff() {
        attackStage = false;
    }
}
