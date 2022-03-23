package sources.modulators;

import sources.Triggerable;

public interface TriggerableEnvelope extends Envelope, Triggerable {
    @Override
    default void gateOn() {
        trigger();
    }

    default void gateOff() {
    }
}
