package Sources;

public interface SignalSource {
    int sampleRate = 44100;
    double samplingPeriod = 1.0/sampleRate;
    double maxFrequency = 20000;

    double getSample(int sampleId);
}
