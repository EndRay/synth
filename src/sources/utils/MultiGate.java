package sources.utils;

import sources.Gated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiGate implements Gated {

    List<Gated> destinations = new ArrayList<>();

    public MultiGate(Gated... destinations) {
        this.destinations.addAll(Arrays.asList(destinations));
    }

    public void addDestination(Gated destination){
        destinations.add(destination);
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
