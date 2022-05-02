package ui.cui;

import ui.SynthMidiReceiver;
import ui.synthcontrollers.AutoMapSynthController;

public class AutoMapSynthMidiReceiver extends SynthMidiReceiver<AutoMapSynthController> {
    public void startMapping(int channel){
        for(AutoMapSynthController synth : synths.get(channel))
            synth.startMapping();
    }
    public void stopMapping(int channel){
        for(AutoMapSynthController synth : synths.get(channel))
            synth.stopMapping();
    }

    @Override
    public void clearSynthControllers(int channel) {
        stopMapping(channel);
        super.clearSynthControllers(channel);
    }
}
