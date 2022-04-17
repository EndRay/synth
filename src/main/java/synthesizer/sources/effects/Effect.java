package synthesizer.sources.effects;

import synthesizer.sources.SignalProcessor;
import synthesizer.sources.utils.Socket;

public interface Effect extends SignalProcessor {
    Socket wet();
}
