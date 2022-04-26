package ui.synthcontrollers;

import synthesizer.VoiceDistributor;
import ui.cui.CCSourceValuesHandler;

public class AutoMapSynthController implements SynthController{

    private final VoiceDistributor distributor;
    private final CCSourceValuesHandler handler;

    public AutoMapSynthController(VoiceDistributor distributor, CCSourceValuesHandler handler){
        this.distributor = distributor;
        this.handler = handler;
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
    public void midiCC(int CC, int value) {
        handler.midiCC(CC, value);
    }

    public void startMapping(){
        handler.startMapping();
    }

    public void stopMapping(){
        handler.stopMapping();
    }
}
