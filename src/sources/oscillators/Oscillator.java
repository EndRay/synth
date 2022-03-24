package sources.oscillators;

import sources.SignalSource;
import sources.utils.Socket;

/**
 * -1 <= getSample(id) <= 1
 */

public interface Oscillator extends SignalSource {
    Socket frequency();
}
