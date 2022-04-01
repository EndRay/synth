package sources;

abstract public class AbstractSignalSource implements SignalSource {
    int lastSampleId;

    public AbstractSignalSource() {
        lastSampleId = -1;
    }

    protected boolean checkAndUpdateSampleId(int sampleId) {
        if (lastSampleId != sampleId) {
            lastSampleId = sampleId;
            return true;
        }
        return false;
    }
}
