package ui.synthcontrollers;

import synthesizer.VoiceDistributor;

import java.util.function.Predicate;

public class SimpleSynthController implements SynthController{
    protected final VoiceDistributor distributor;
    protected Predicate<Integer> condition = note -> true;

    public SimpleSynthController(VoiceDistributor distributor){
        this.distributor = distributor;
    }

    @Override
    public void noteOn(int note, int velocity) {
        if(condition.test(note))
            distributor.noteOn(note, velocity);
    }

    @Override
    public void noteOff(int note, int velocity) {
        if(condition.test(note))
            distributor.noteOff(note, velocity);
    }

    @Override
    public void setCondition(Predicate<Integer> condition){
        this.condition = condition;
    }

    @Override
    public void allNotesOff() {
        distributor.allNotesOff();
    }

    @Override
    public void pitchbend(int value) {
        distributor.pitchbend(value);
    }

    @Override
    public void midiCC(int CC, int value) {
        if(CC == 1)
            distributor.modwheel(value);
    }
}
