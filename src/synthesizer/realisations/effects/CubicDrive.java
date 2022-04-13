package synthesizer.realisations.effects;

import synthesizer.sources.SignalSource;
import synthesizer.sources.effects.AbstractDistortion;

public class CubicDrive extends AbstractDistortion {

    public CubicDrive(){ super(); }
    public CubicDrive(double gain){ super(gain); }
    public CubicDrive(SignalSource source){ super(source); }
    public CubicDrive(SignalSource source, double gain){ super(source, gain); }

    @Override
    protected double distortFunction(double x) {
        if(x <= -1)
            return -2.0/3;
        if(x >= 1)
            return 2.0/3;
        return x - x*x*x/3;
    }
}
