package structscript;

import synthesizer.sources.utils.SourceValue;

public interface SourceValuesHandler {
    void addSourceValue(SourceValue sourceValue);

    default void addSection(String name){}
}
