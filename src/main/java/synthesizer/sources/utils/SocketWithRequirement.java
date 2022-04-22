package synthesizer.sources.utils;

import synthesizer.sources.SignalProcessor;
import synthesizer.sources.SignalSource;

public class SocketWithRequirement<T extends SignalSource> extends Socket {

    Class<T> requirement;

    public SocketWithRequirement(T source, Class<T> requirement){
        super(source);
        this.requirement = requirement;
    }

    @Override
    public T getSource(){
        return requirement.cast(source);
    }

    public void bind(SignalSource source) throws ClassCastException {
        if(!requirement.isAssignableFrom(source.getClass()))
            throw new ClassCastException();
        super.bind(source);
    }

    public void modulate(SignalSource modulator){
        throw new UnsupportedOperationException();
    }

    public void process(SignalProcessor processor){
        throw new UnsupportedOperationException();
    }

}
