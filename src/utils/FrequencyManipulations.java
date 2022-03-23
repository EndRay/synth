package utils;

import sources.SignalSource;
import sources.utils.DC;

import static java.lang.Math.pow;

public class FrequencyManipulations {
    private FrequencyManipulations(){}

    public static double getFrequency(double frequency) {
        return SignalSource.frequencyToVoltage(frequency);
    }

    public static double getSemitonesShift(double semitones){
        return pow(2, semitones/12);
    }

    public static double getFrequencyBySemitones(double semitones) {
        return 440 * pow(2, (semitones-69)/12);
    }
}
