package sequencer;

public interface Clockable {
    void ping();
    void start();

    void stop();
}
