package synthesizer.sources.utils;

import synthesizer.sources.SignalProcessor;
import synthesizer.sources.SignalSource;

import static synthesizer.sources.SignalSource.frequencyToVoltage;

public interface PseudoSocket extends SignalSource {
    default void set(double value){
        bind(new DC(value));
    }
    default void setFrequency(double value){
        set(frequencyToVoltage(value));
    }

    void bind(SignalSource source);

    void modulate(SignalSource modulator);

    void process(SignalProcessor processor);
}
