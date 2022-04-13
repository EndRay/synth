package synthesizer.sources;

import synthesizer.sources.utils.Socket;

public interface SignalProcessor extends SignalSource {
    Socket source();
}
