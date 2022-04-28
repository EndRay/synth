package synthesizer.realisations.effects;

import synthesizer.sources.AbstractSignalProcessor;
import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.DC;
import synthesizer.sources.utils.Socket;

import static java.lang.Math.pow;

public class Reverb extends AbstractSignalProcessor {
    final public int tapeLength = (int) (5 * sampleRate);
    public static final double dieCoefficient = 0.01;
    private int ptr = 0;
    //private final double[] delays = new double[]{};
    private final int[] sampleDelays = new int[]{4799, 4999, 5399, 5801, 3287, 2839};
//    private final int[] delays = new int[]{4799, 4999, 5399, 5801};
    private final int tapesCount = sampleDelays.length;
    private final double[][] tapes = new double[tapesCount][tapeLength];
    private final Socket decay;
    {
        for(int t = 0; t < tapesCount; ++t) {
            for (int i = 0; i < tapeLength; ++i)
                tapes[t][i] = 0;
        }
    }

    public Reverb() {
        this(DC.getSecondsDC(2));
    }
    public Reverb(SignalSource decaySource) {
        decay = new Socket(decaySource);
    }

    public Socket decay(){
        return decay;
    }

    @Override
    protected double recalculate(int sampleId) {
        ++ptr;
        if (ptr >= tapeLength)
            ptr = 0;
        double res = 0;
        double length = decay().getTime(sampleId);
        double coefPerSample = pow(dieCoefficient, 1/length/sampleRate);
        double sourceSample = source().getSample(sampleId);
        for(int i = 0; i < tapesCount; ++i){
            int d = sampleDelays[i];
            double c = pow(coefPerSample, d);
            int delayedPtr = ptr - d;
            if (delayedPtr < 0)
                delayedPtr += tapeLength;
            tapes[i][ptr] = c * (tapes[i][delayedPtr] + sourceSample);
            res += c * tapes[i][delayedPtr];
        }
        return res / tapesCount;
    }
}
