package sources.voices;

import sources.*;
import sources.utils.Socket;
import sources.utils.SourceValue;

import static utils.FrequencyManipulations.getFrequencyBySemitones;

public class Voice extends AbstractSignalProcessor {

    SourceValue pitch, velocity, aftertouch, releaseVelocity, gate;

    public Voice(SourceValue pitch, SourceValue velocity, SourceValue aftertouch, SourceValue releaseVelocity, SourceValue gated){
        this(new Socket(), pitch, velocity, aftertouch, releaseVelocity, gated);
    }
    public Voice(SignalSource source, SourceValue pitch, SourceValue velocity, SourceValue aftertouch, SourceValue releaseVelocity, SourceValue gate) {
        super(source);
        this.pitch = pitch;
        this.velocity = velocity;
        this.aftertouch = aftertouch;
        this.releaseVelocity = releaseVelocity;
        this.gate = gate;
    }

    @Override
    public double getSample(int sampleId) {
        return source().getSample(sampleId);
    }

    public void noteOn(int note){
        noteOn(note, 64);
    }

    public void noteOn(int note, int velocity) {
        pitch.setValue(SignalSource.frequencyToVoltage(getFrequencyBySemitones(note)));
        this.velocity.setValue(velocity / 128.0);
        gate.setValue(1);
    }

    public void noteOff(){
        noteOff(0);
    }

    public void noteOff(int velocity) {
        releaseVelocity.setValue(velocity / 128.0);
        gate.setValue(0);
    }
}
