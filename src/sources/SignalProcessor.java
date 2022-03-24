package sources;

import sources.utils.Socket;

public interface SignalProcessor extends SignalSource {
    Socket source();
}
