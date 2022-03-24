package sources.filters;

import sources.SignalProcessor;
import sources.oscillators.Oscillator;
import sources.utils.Socket;

public interface Filter extends SignalProcessor, Oscillator {

    Socket frequency();

    default void open() {
        frequency().setFrequency(maxFrequency);
    }
}
