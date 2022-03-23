package sources.modulators;

import sources.Gated;
import sources.SignalSource;

public interface Envelope extends SignalSource, Gated {
    void gateOn();
    void gateOff();
}
