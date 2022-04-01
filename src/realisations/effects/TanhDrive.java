package realisations.effects;

import sources.SignalSource;
import sources.effects.AbstractDistortion;

import static java.lang.Math.tanh;

public class TanhDrive extends AbstractDistortion {

    public TanhDrive(){ super(); }
    public TanhDrive(double gain){ super(gain); }
    public TanhDrive(SignalSource source){ super(source); }
    public TanhDrive(SignalSource source, double gain){ super(source, gain); }

    @Override
    protected double distortFunction(double x) {
        return tanh(x);
    }
}
