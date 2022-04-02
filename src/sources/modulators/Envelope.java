package sources.modulators;

import sources.SignalSource;
import sources.utils.Socket;

public interface Envelope extends SignalSource {
    Socket gate();
}
