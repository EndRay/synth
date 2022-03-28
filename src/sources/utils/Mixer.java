package sources.utils;

import sources.AbstractSignalSource;
import sources.SignalProcessor;
import sources.SignalSource;

import java.util.ArrayList;
import java.util.List;

public class Mixer extends AbstractSignalSource implements PseudoSocket {

    private final List<Socket> sources;
    double lastSample;

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
    public double getSample(int sampleId) {
        if (checkAndUpdateSampleId(sampleId)) {
            lastSample = 0;
            for(int i = 0; i < size(); ++i)
                lastSample += get(i).getSample(sampleId);
        }
        return lastSample;
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
