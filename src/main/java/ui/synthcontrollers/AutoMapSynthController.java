package ui.synthcontrollers;

import synthesizer.VoiceDistributor;
import ui.cui.CCSourceValuesHandler;

public class AutoMapSynthController extends SimpleSynthController implements SynthController{

    private final CCSourceValuesHandler handler;

    public AutoMapSynthController(VoiceDistributor distributor, CCSourceValuesHandler handler){
        super(distributor);
        this.handler = handler;
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
