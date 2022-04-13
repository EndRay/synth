package synthesizer.realisations.effects;

import synthesizer.sources.effects.AbstractEffect;

import static java.lang.Math.pow;

public class Reverb extends AbstractEffect {
    final public int tapeLength = (int) (5 * sampleRate);
    private int ptr = 0;
    private final double coefPerSecond = 0.06443;
    private final int[] delays = new int[]{4799, 4999, 5399, 5801, 3287, 2839};
//    private final int[] delays = new int[]{4799, 4999, 5399, 5801};
    private final int tapesCount = delays.length;
    private final double[] coefs = new double[tapesCount];
    private final double[][] tapes = new double[tapesCount][tapeLength];
    {
        for(int t = 0; t < tapesCount; ++t) {
            for (int i = 0; i < tapeLength; ++i)
                tapes[t][i] = 0;
            coefs[t] = pow(coefPerSecond, (double)delays[t]/sampleRate);
        }
    }

    public Reverb() {
    }

    public Reverb(double wetness) {
        super(wetness);
    }

    @Override
    protected double getWetSample(int sampleId) {
        ++ptr;
        if (ptr >= tapeLength)
            ptr = 0;
        double res = 0;
        double sourceSample = source().getSample(sampleId);
        for(int i = 0; i < delays.length; ++i){
            int d = delays[i];
            double c = coefs[i];
            int delayedPtr = ptr - d;
            if (delayedPtr < 0)
                delayedPtr += tapeLength;
            tapes[i][ptr] = c * (tapes[i][delayedPtr] + sourceSample);
            res += c * tapes[i][delayedPtr];
        }
        return res / tapesCount;

    }
}
