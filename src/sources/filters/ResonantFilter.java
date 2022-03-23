package sources.filters;

import sources.SignalSource;

public interface ResonantFilter extends Filter {
    double getResonance(int sampleId);

    void setResonance(SignalSource resonanceSource);
}
