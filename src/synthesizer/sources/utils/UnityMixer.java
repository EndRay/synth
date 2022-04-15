package synthesizer.sources.utils;

import synthesizer.sources.SignalProcessor;
import synthesizer.sources.SignalSource;

import java.util.ArrayList;
import java.util.List;

public class UnityMixer extends Mixer implements PseudoSocket{

    public UnityMixer(){ super(0); }
    //public UnityMixer(int size){ super(size); }
    public UnityMixer(SignalSource... sources) {
        super(sources);
    }

    protected UnityMixer(UnityMixer mixer){
        super(mixer);
    }

    @Override
    protected UnityMixer copy(){
        return new UnityMixer(this);
    }

    @Override
    protected double recalculate(int sampleId) {
        double res = 0;
        for(int i = 0; i < size(); ++i)
            res += get(i).getSample(sampleId);
        res /= size();
        return res;
    }
}
