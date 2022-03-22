package Sources;

import Sources.Utils.Attenuator;
import Sources.Utils.DC;

public interface SignalSource {
    int sampleRate = 44100;
    double samplingPeriod = 1.0 / sampleRate;
    double minFrequency = 1, maxFrequency = 20000;

    double getSample(int sampleId);

    static double frequencyToVoltage(double frequency) {
        return Math.log(frequency / minFrequency) / Math.log(maxFrequency / minFrequency);
    }

    static double voltageToFrequency(double voltage) {
        return Math.pow(maxFrequency / minFrequency, voltage) * minFrequency;
    }

    default SignalSource attenuated(double coefficient){
        return new Attenuator(this, new DC(coefficient));
    }

    default SignalSource attenuated(SignalSource coefficientSource){
        return new Attenuator(this, coefficientSource);
    }
}
