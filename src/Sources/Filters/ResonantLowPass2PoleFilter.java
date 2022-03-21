package Sources.Filters;

import Sources.SignalSource;

public class ResonantLowPass2PoleFilter extends AbstractFilter implements ResonantFilter {

    double resonance;
    double currentSample, currentSample2;

    public ResonantLowPass2PoleFilter(SignalSource source) {
        super(source);
        setResonance(0);
    }

    public ResonantLowPass2PoleFilter(SignalSource source, double frequency) {
        super(source, frequency);
        setResonance(0);
    }

    public ResonantLowPass2PoleFilter(SignalSource source, double frequency, double resonance) {
        super(source, frequency);
        setResonance(resonance);
    }

    @Override
    public double getSample(int sampleId) {
        if(checkAndUpdateSampleId(sampleId)){
            double f = getAlpha();
            currentSample2 = currentSample2 + f * (source.getSample(sampleId) - currentSample2 + getFeedback() * (currentSample2 - currentSample));
            currentSample = currentSample + f * (currentSample2 - currentSample);
        }
        return currentSample;
    }


    @Override
    public double getResonance() {
        return resonance;
    }

    @Override
    public void setResonance(double resonance) {
        this.resonance = resonance;
    }

    public double getFeedback() {
        return resonance + resonance / (1.0 - getAlpha());
    }
}
