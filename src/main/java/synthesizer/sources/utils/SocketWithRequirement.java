package synthesizer.sources.utils;

import synthesizer.sources.SignalSource;

public class SocketWithRequirement<T extends SignalSource> extends Socket {

    T source;

    public SocketWithRequirement(T source){
        this.source = source;
    }
    public T getSource(){
        return source;
    }
}
