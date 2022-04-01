package realisations.effects;

import sources.SignalSource;
import sources.effects.AbstractDistortion;

public class BasicWavefolder extends AbstractDistortion {

    public BasicWavefolder(){ super(); }
    public BasicWavefolder(double gain){ super(gain); }
    public BasicWavefolder(SignalSource source){ super(source); }
    public BasicWavefolder(SignalSource source, double gain){ super(source, gain); }

    @Override
    protected double distortFunction(double x) {
        if(x <= -1)
            return -2.0/3;
        if(x >= 1)
            return 2.0/3;
        return x - x*x*x/3;
    }
}
