package synthesizer.sources.effects;

import synthesizer.sources.AbstractSignalSource;
import synthesizer.sources.SignalProcessor;
import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.DC;
import synthesizer.sources.utils.Socket;
import synthesizer.sources.utils.SocketWithRequirement;

public class DryWet extends AbstractSignalSource implements SignalProcessor {
    SocketWithRequirement<SignalProcessor> effect;
    Socket wet;

    public DryWet(SignalProcessor effect){
        this(effect, 0);
    }
    public DryWet(SignalProcessor effect, double wet){
        this(effect, new DC(wet));
    }
    public DryWet(SignalProcessor effect, SignalSource wetSource){
        this.effect = new SocketWithRequirement<>(effect, SignalProcessor.class);
        this.wet = new Socket(wetSource);
    }

    public SocketWithRequirement<SignalProcessor> effect(){
        return effect;
    }
    public Socket wet(){
        return wet;
    }
    @Override
    public Socket source() {
        return effect().getSource().source();
    }

    @Override
    protected double recalculate(int sampleId) {
        double wetness = wet().getSample(sampleId);
        double wetSample = effect().getSample(sampleId), drySample = source().getSample(sampleId);
        return drySample + (wetSample - drySample) * wetness;
    }
}
