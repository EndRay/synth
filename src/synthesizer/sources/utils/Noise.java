package synthesizer.sources.utils;

import synthesizer.sources.AbstractSignalSource;

import java.util.Random;

public class Noise extends AbstractSignalSource {
    private final Random rd = new Random();
    private double lastSample = 0;


    @Override
    protected double recalculate(int sampleId) {
        return 2 * (rd.nextDouble() - 0.5);
    }
}
