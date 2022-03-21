package Sources.Filters;

import Sources.Oscillators.Oscillator;
import Sources.SignalSource;

public interface Filter extends SignalSource, Oscillator {
    double getFrequency();
    void setFrequency(double frequency);
    default void open(){
        setFrequency(maxFrequency);
    }
}
