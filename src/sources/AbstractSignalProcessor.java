package sources;

import sources.utils.DC;

abstract public class AbstractSignalProcessor extends AbstractSignalSource implements SignalProcessor {

    private SignalSource source;

    public AbstractSignalProcessor(){
        this(new DC());
    }

    public AbstractSignalProcessor(SignalSource source){
        this.source = source;
    }

    @Override
    public double getSourceSample(int sampleId){
        return source.getSample(sampleId);
    }

    @Override
    public void setSignalSource(SignalSource source){
        this.source = source;
    }

    @Override
    public SignalSource getSignalSource(){
        return source;
    }
}
