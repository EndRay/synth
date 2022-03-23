package sources.filters;

import sources.SignalSource;
import sources.utils.DC;

public class ResonantLowPass2PoleFilter extends AbstractFilter implements ResonantFilter {
    SignalSource resonanceSource;
    double[] buf;

    public ResonantLowPass2PoleFilter(SignalSource source) {
        super(source);
        setResonance(new DC(0));
        buf = new double[2];
        buf[0] = 0;
        buf[1] = 0;
    }

    public ResonantLowPass2PoleFilter(SignalSource source, SignalSource frequencySource) {
        this(source, frequencySource, new DC(0));
    }

    public ResonantLowPass2PoleFilter(SignalSource source, SignalSource frequencySource, SignalSource resonanceSource) {
        super(source, frequencySource);
        setResonance(resonanceSource);
        buf = new double[2];
        buf[0] = 0;
        buf[1] = 0;
    }

    @Override
    public double getSample(int sampleId) {
        if (checkAndUpdateSampleId(sampleId)) {
            double f = getAlpha(sampleId);
            buf[0] = buf[0] + f * (source.getSample(sampleId) - buf[0] + getFeedback(sampleId) * (buf[0] - buf[1]));
            buf[1] = buf[1] + f * (buf[0] - buf[1]);
        }
        return buf[1];
    }


    @Override
    public double getResonance(int sampleId) {
        return resonanceSource.getSample(sampleId);
    }

    @Override
    public void setResonance(SignalSource resonanceSource) {
        this.resonanceSource = resonanceSource;
    }

    public double getFeedback(int sampleId) {
        double q = getResonance(sampleId);
        return q + q / (1.0 - getAlpha(sampleId));
    }
}
