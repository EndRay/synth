package sources.utils;

import sources.SignalSource;

import static utils.FrequencyManipulations.getFrequencyBySemitones;
import static utils.FrequencyManipulations.getSemitonesShift;

public class DC implements SignalSource {

    final double offset;

    public DC() {
        this(0);
    }

    public DC(double offset) {
        this.offset = offset;
    }

    public double getOffset() {
        return offset;
    }

    public static DC getFrequencyDC(double frequency) {
        return new DC(SignalSource.frequencyToVoltage(frequency));
    }

    public static DC getFrequencyCoefficientDC(double frequencyCoefficient){
        return new DC(SignalSource.frequencyCoefficientToVoltage(frequencyCoefficient));
    }

    public static DC getSemitonesShiftDC(double semitones){
        return getFrequencyCoefficientDC(getSemitonesShift(semitones));
    }

    public static DC getFrequencyDCBySemitones(double semitones) {
        return getFrequencyDC(getFrequencyBySemitones(semitones));
    }


    @Override
    public double getSample(int sampleId) {
        return getOffset();
    }
}
