package synthesizer.sources.modulators;

import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.Socket;

public interface Envelope extends SignalSource {
    Socket gate();
}
