package sources.utils;

import sources.Gated;

public class MultiGate implements Gated {

    Gated[] destinations;

    public MultiGate(Gated... destinations) {
        this.destinations = destinations;
    }

    @Override
    public void gateOn() {
        for(Gated destination : destinations)
            destination.gateOn();
    }

    @Override
    public void gateOff() {
        for(Gated destination : destinations)
            destination.gateOff();
    }
}
