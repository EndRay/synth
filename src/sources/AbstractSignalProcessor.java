package sources;

import sources.utils.SourceValue;

abstract public class AbstractSignalProcessor extends AbstractSignalSource implements SignalProcessor {

    private SignalSource source;

    public AbstractSignalProcessor(SignalSource source){
        this.source = source;
    }

    @Override
    public double getSourceSample(int sampleId){
        return source.getSample(sampleId);
    }

    @Override
    public void setSoundSource(SignalSource source){
        this.source = source;
    }

}
