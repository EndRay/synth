package synthesizer.realisations.modulators;

import synthesizer.sources.AbstractSignalSource;
import synthesizer.sources.SignalSource;
import synthesizer.sources.modulators.Envelope;
import synthesizer.sources.utils.DC;
import synthesizer.sources.utils.Socket;

import static java.lang.Math.*;

public class AR extends AbstractSignalSource implements Envelope {
    private final Socket attack, release, gate = new Socket();
    private double currentSample;


    public AR(double attack,double release) {
        this(new DC(attack), new DC(release));
    }
    public AR(SignalSource attackSource, SignalSource releaseSource) {
        attack = new Socket(attackSource);
        release = new Socket(releaseSource);
        currentSample = 0;
    }

    public Socket attack(){
        return attack;
    }
    public Socket release(){
        return release;
    }
    public Socket gate(){ return gate; }

    @Override
    protected double recalculate(int sampleId) {
        boolean g = gate().getGate(sampleId);
        if (g)
            currentSample = min(currentSample + 1 / attack().getTime(sampleId) / sampleRate, 1);
        else currentSample = max(currentSample - 1 / release().getTime(sampleId) / sampleRate, 0);
        return currentSample;
    }
}
