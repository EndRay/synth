package synthesizer.sources.filters;

import synthesizer.sources.SignalProcessor;
import synthesizer.sources.oscillators.Oscillator;
import synthesizer.sources.utils.Socket;

public interface Filter extends SignalProcessor, Oscillator {

    Socket frequency();

    default void open() {
        frequency().setFrequency(maxFrequency);
    }
}
