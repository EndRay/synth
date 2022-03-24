package sources;

import sources.utils.Socket;

abstract public class AbstractSignalProcessor extends AbstractSignalSource implements SignalProcessor {

    private final Socket source = new Socket();

    public AbstractSignalProcessor(){}

    public AbstractSignalProcessor(SignalSource source){
        this.source.bind(source);
    }

    @Override
    public Socket source(){
        return source;
    }
}
