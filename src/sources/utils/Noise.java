package sources.utils;

import sources.AbstractSignalSource;

import java.util.Random;

public class Noise extends AbstractSignalSource {
    private final Random rd = new Random();
    private double lastSample = 0;

    @Override
    public double getSample(int sampleId) {
        if (checkAndUpdateSampleId(sampleId))
            lastSample = 2 * (rd.nextDouble() - 0.5);
        return lastSample;
    }
}
