package synthesizer.sources.utils;

import synthesizer.sources.SignalSource;

import static synthesizer.sources.SignalSource.voltageToFrequency;
import static synthesizer.utils.FrequencyManipulations.getFrequencyBySemitones;
import static synthesizer.utils.FrequencyManipulations.getSemitonesShift;

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
    public static DC getSecondsDC(double seconds) {
        return new DC(SignalSource.timeToVoltage(seconds));
    }

    public static DC getFrequencyRatioDC(double frequencyRatio) {
        return new DC(SignalSource.frequencyRatioToVoltage(frequencyRatio));
    }

    public static DC getSemitonesShiftDC(double semitones) {
        return getFrequencyRatioDC(getSemitonesShift(semitones));
    }

    public static DC getFrequencyDCBySemitones(double semitones) {
        return getFrequencyDC(getFrequencyBySemitones(semitones));
    }

    @Override
    public double getSample(int sampleId) {
        return getOffset();
    }

    public SignalSource attenuate(SignalSource coefficientSource) {
        if(coefficientSource instanceof DC anotherDC)
            return new DC(getOffset() * anotherDC.getOffset());
        return new Attenuator(this, coefficientSource);
    }

    public SignalSource add(SignalSource valueSource) {
        if(valueSource instanceof DC anotherDC)
            return new DC(getOffset() + anotherDC.getOffset());
        return new Mixer(this, valueSource);
    }

    public SignalSource addFrequency(double frequency) {
        return getFrequencyDC(voltageToFrequency(getOffset() + frequency));
    }
}
