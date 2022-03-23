import sources.SignalSource;
import sources.modulators.Envelope;
import sources.utils.DC;
import sources.utils.SourceValue;

import static utils.FrequencyManipulations.getFrequencyBySemitones;

public class MyMonoSynth implements Synth{

    SignalSource voice;
    SourceValue pitch;
    Envelope envelope;
    int note;

    public MyMonoSynth(SignalSource voice, SourceValue pitch, Envelope envelope){
        this.voice = voice;
        this.pitch = pitch;
        this.envelope = envelope;
        note = -1;
    }

    @Override
    public void noteOn(int note, double velocity) {
        pitch.setValue(SignalSource.frequencyToVoltage(getFrequencyBySemitones(note)));
        this.note = note;
        envelope.gateOn();
    }

    @Override
    public void noteOff(int note, double velocity) {
        if(note == this.note)
            envelope.gateOff();
    }


    @Override
    public double getSample(int sampleId) {
        return voice.getSample(sampleId);
    }
}
