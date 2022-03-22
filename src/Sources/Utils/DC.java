package Sources.Utils;

import Sources.SignalSource;

public class DC implements SignalSource {

    double offset;

    public DC(double offset) {
        this.offset = offset;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public void setFrequency(double frequency) {
        this.offset = SignalSource.frequencyToVoltage(frequency);
    }

    public static DC getFrequencyDC(double frequency) {
        return new DC(SignalSource.frequencyToVoltage(frequency));
    }

    @Override
    public double getSample(int sampleId) {
        return offset;
    }
}
