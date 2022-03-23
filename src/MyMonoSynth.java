import sources.Gated;
import sources.SignalSource;
import sources.modulators.Envelope;
import sources.utils.DC;
import sources.utils.SourceValue;

import static utils.FrequencyManipulations.getFrequencyBySemitones;

public class MyMonoSynth implements Synth{

    SignalSource voice;
    SourceValue pitch;
    Gated gate;
    int note;

    public MyMonoSynth(SignalSource voice, SourceValue pitch, Gated gate){
        this.voice = voice;
        this.pitch = pitch;
        this.gate = gate;
        note = -1;
    }

    @Override
    public void noteOn(int note, double velocity) {
        pitch.setValue(SignalSource.frequencyToVoltage(getFrequencyBySemitones(note)));
        this.note = note;
        gate.gateOn();
    }

    @Override
    public void noteOff(int note, double velocity) {
        if(note == this.note)
            gate.gateOff();
    }


    @Override
    public double getSample(int sampleId) {
        return voice.getSample(sampleId);
    }
}
