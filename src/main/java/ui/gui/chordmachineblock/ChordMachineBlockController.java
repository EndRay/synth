package ui.gui.chordmachineblock;

import sequencer.Step;
import ui.gui.sequencer.SequencerPanelController;

import javax.sound.midi.Receiver;

public class ChordMachineBlockController {
    public SequencerPanelController sequencerPanelController;
    ChordMachineBlockController(Receiver receiver){
        sequencerPanelController = new SequencerPanelController(receiver);
        sequencerPanelController.initialize();
    }
}
