package sources.utils;

import sources.SignalSource;

import static sources.SignalSource.frequencyToVoltage;

public interface PseudoSocket extends SignalSource {
    default void set(double value){
        bind(new DC(value));
    }
    default void setFrequency(double value){
        set(frequencyToVoltage(value));
    }

    void bind(SignalSource source);

    void modulate(SignalSource modulator);
}
