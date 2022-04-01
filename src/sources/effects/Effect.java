package sources.effects;

import sources.SignalProcessor;
import sources.utils.Socket;

public interface Effect extends SignalProcessor {
    double getWetSample(int sampleId);
    Socket wet();
}
