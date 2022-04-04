package sources.utils;

import sources.AbstractSignalSource;
import sources.SignalProcessor;
import sources.SignalSource;

import java.util.ArrayList;
import java.util.List;

public class Mixer extends AbstractSignalSource implements PseudoSocket {

    private final List<Socket> sources;

    public Mixer(int size){
        sources = new ArrayList<>(size);
        for(int i = 0; i < size; ++i)
            sources.add(new Socket());
    }

    public Mixer(SignalSource... sources) {
        this.sources = new ArrayList<>(sources.length);
        for(int i = 0; i < sources.length; ++i) {
            this.sources.add(new Socket());
            this.sources.get(i).bind(sources[i]);
        }
    }

    public Socket get(int index){
        return sources.get(index);
    }

    public int size(){
        return sources.size();
    }

    @Override
    protected double recalculate(int sampleId) {
        double res = 0;
        for(int i = 0; i < size(); ++i)
            res += get(i).getSample(sampleId);
        return res;
    }

    @Override
    public void bind(SignalSource source) {
        sources.clear();
        sources.add(new Socket(source));
    }

    @Override
    public void modulate(SignalSource modulator) {
        sources.add(new Socket(modulator));
    }
}
