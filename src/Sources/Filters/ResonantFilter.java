package Sources.Filters;

import Sources.SignalSource;

public interface ResonantFilter extends Filter{
    double getResonance(int sampleId);
    void setResonance(SignalSource resonanceSource);
}
