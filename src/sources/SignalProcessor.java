package sources;

public interface SignalProcessor extends SignalSource  {
    double getSourceSample(int sampleId);
    void setSoundSource(SignalSource source);

}
