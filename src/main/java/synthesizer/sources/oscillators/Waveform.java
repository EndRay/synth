package synthesizer.sources.oscillators;

import synthesizer.sources.SignalSource;

/**
 * desirable to have getSample giving this waveform with tuningFrequency frequency
 */

public interface Waveform extends SignalSource {
    double getAmplitude(int sampleId, double ptr);
}
