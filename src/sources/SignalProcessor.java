package sources;

public interface SignalProcessor extends SignalSource  {
    double getSourceSample(int sampleId);
    void setSignalSource(SignalSource source);
    SignalSource getSignalSource();
    default void preprocess(SignalProcessor processor){
        processor.setSignalSource(getSignalSource());
        setSignalSource(processor);
    }
}
