package sources;

abstract public class AbstractSignalSource implements SignalSource {
    private int lastSampleId;
    private double lastSample = 0;

    public AbstractSignalSource() {
        lastSampleId = -1;
    }

    protected abstract double recalculate(int sampleId);

    private boolean checkAndUpdateSample(int sampleId) {
        if (lastSampleId != sampleId) {
            lastSample = recalculate(sampleId);
            lastSampleId = sampleId;
            return true;
        }
        return false;
    }

    @Override
    public double getSample(int sampleId){
        checkAndUpdateSample(sampleId);
        return lastSample;
    }
}
