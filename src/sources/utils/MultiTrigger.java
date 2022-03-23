package sources.utils;

import sources.Triggerable;

public class MultiTrigger implements Triggerable {

    Triggerable[] destinations;

    public MultiTrigger(Triggerable... destinations) {
        this.destinations = destinations;
    }

    @Override
    public void trigger() {
        for(Triggerable destination : destinations)
            destination.trigger();
    }
}
