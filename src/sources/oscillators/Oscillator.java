package sources.oscillators;

import sources.SignalSource;

/**
 * -1 <= getSample(id) <= 1
 */

public interface Oscillator extends SignalSource {
    double getFrequency(int sampleId);

    void setFrequency(SignalSource frequencySource);

    @Override
    default SignalSource map(double min, double max){
        return this.attenuated((max-min)/2).add((min+max)/2);
    }
}
