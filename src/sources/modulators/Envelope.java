package sources.modulators;

import sources.SignalSource;

public interface Envelope extends SignalSource {
    void gateOn();
    void gateOff();
}
