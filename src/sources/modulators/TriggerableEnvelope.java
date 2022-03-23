package sources.modulators;

import sources.Triggerable;

public interface TriggerableEnvelope extends Envelope, Triggerable {
    @Override
    default void trigger() {
        gateOff();
        gateOn();
    }
}
