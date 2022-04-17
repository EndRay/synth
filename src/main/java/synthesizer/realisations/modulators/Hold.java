package synthesizer.realisations.modulators;

import synthesizer.sources.AbstractSignalSource;
import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.Socket;

public class Hold extends AbstractSignalSource {

    final private Socket hold = new Socket(), trigger = new Socket();
    private boolean lastTrigger = false;
    private double samplesPassed = -1;
    private double lastSample = 0;

    public Hold(double hold){
        hold().set(hold);
    }
    public Hold(SignalSource holdSource){
        hold().bind(holdSource);
    }

    public Socket hold(){
        return hold;
    }

    public Socket trigger(){
        return trigger;
    }

    @Override
    protected double recalculate(int sampleId) {
        ++samplesPassed;
        boolean t = trigger().getGate(sampleId);
        if(!lastTrigger && t) {
            samplesPassed = 0;
            lastSample = 1;
        }
        lastTrigger = t;
        if(samplesPassed > hold().getTime(sampleId) * sampleRate)
            lastSample = 0;
        return lastSample;
    }
}
