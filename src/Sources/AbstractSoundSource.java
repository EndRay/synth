package Sources;

abstract public class AbstractSoundSource implements SignalSource {
    int lastSampleId;

    public AbstractSoundSource() {
        lastSampleId = -1;
    }

    public int getLastSampleId() {
        return lastSampleId;
    }

    protected void updateLastSampleId(int sampleId) {
        lastSampleId = sampleId;
    }

    public boolean isNextSample(int sampleId) {
        return lastSampleId != sampleId;
    }

    protected boolean checkAndUpdateSampleId(int sampleId) {
        if (lastSampleId != sampleId) {
            lastSampleId = sampleId;
            return true;
        }
        return false;
    }
}
