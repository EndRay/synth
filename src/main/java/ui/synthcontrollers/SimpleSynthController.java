package ui.synthcontrollers;

import synthesizer.VoiceDistributor;

public class SimpleSynthController implements SynthController{
    protected final VoiceDistributor distributor;

    public SimpleSynthController(VoiceDistributor distributor){
        this.distributor = distributor;
    }

    @Override
    public void noteOn(int note, int velocity) {
        distributor.noteOn(note, velocity);
    }

    @Override
    public void noteOff(int note, int velocity) {
        distributor.noteOff(note, velocity);
    }

    @Override
    public void allNotesOff() {
        distributor.allNotesOff();
    }

    @Override
    public void midiCC(int CC, int value) {}
}
