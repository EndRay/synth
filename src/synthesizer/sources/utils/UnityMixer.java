package synthesizer.sources.utils;

import synthesizer.sources.SignalSource;

public class UnityMixer extends Mixer implements PseudoSocket{

    public UnityMixer(){ super(0); }
    //public UnityMixer(int size){ super(size); }
    public UnityMixer(SignalSource... sources) {
        super(sources);
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
