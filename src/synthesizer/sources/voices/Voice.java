package synthesizer.sources.voices;

import synthesizer.sources.utils.SourceValue;
import synthesizer.sources.utils.Triggerable;

import static synthesizer.utils.FrequencyManipulations.getFrequencyBySemitones;

public class Voice {

    SourceValue pitch, velocity, aftertouch, releaseVelocity, gate;
    Triggerable trigger;

    public Voice(SourceValue pitch, SourceValue velocity, SourceValue aftertouch, SourceValue releaseVelocity, SourceValue gate, Triggerable trigger) {
        this.pitch = pitch;
        this.velocity = velocity;
        this.aftertouch = aftertouch;
        this.releaseVelocity = releaseVelocity;
        this.gate = gate;
        this.trigger = trigger;
    }

    public void noteOn(int note){
        noteOn(note, 64);
    }

    public void noteOn(int note, int velocity) {
        pitch.setFrequency(getFrequencyBySemitones(note));
        this.velocity.setValue(velocity / 128.0);
        gate.setValue(1);
        trigger.trigger();
    }

    public void noteOff(){
        noteOff(0);
    }

    public void noteOff(int velocity) {
        releaseVelocity.setValue(velocity / 128.0);
        gate.setValue(0);
    }
}
