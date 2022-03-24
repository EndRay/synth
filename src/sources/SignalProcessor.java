package sources;

public interface SignalProcessor extends SignalSource {
    double getSourceSample(int sampleId);
    void bind(SignalSource source);
    SignalSource getSource();
    default void preprocess(SignalProcessor processor){
        processor.bind(getSource());
        bind(processor);
    }
}
