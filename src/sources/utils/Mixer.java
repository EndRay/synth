package sources.utils;

import sources.AbstractSignalSource;
import sources.SignalSource;

public class Mixer extends AbstractSignalSource {

    private final Socket[] sources;
    double lastSample;

    public Mixer(int size){
        sources = new Socket[size];
        for(int i = 0; i < size; ++i)
            sources[i] = new Socket();
    }

    public Mixer(SignalSource... sources) {
        this.sources = new Socket[sources.length];
        for(int i = 0; i < sources.length; ++i) {
            this.sources[i] = new Socket();
            this.sources[i].bind(sources[i]);
        }
    }

    public Socket get(int index){
        return sources[index];
    }

    public int size(){
        return sources.length;
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
}
