package synthesizer.realisations.effects;

import synthesizer.sources.SignalSource;
import synthesizer.sources.effects.AbstractDistortion;

import static java.lang.Math.tanh;

public class TanhDrive extends AbstractDistortion {

    public TanhDrive(){ super(); }
    public TanhDrive(SignalSource source){ super(source); }

    @Override
    protected double distortFunction(double x) {
        return tanh(x);
    }
}
