package synthesizer.sources.oscillators;

import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.Socket;

/**
 * -1 <= getSample(id) <= 1
 */

public interface Oscillator extends SignalSource {
    Socket frequency();
}
