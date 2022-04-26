package ui.synthcontrollers;

public interface SynthController {
    void noteOn(int note, int velocity);
    void noteOff(int note, int velocity);
    void allNotesOff();

    default void noteOn(int note){
        noteOn(note, 64);
    }
    default void noteOff(int note){
        noteOff(note, 0);
    }

    void midiCC(int CC, int value);
}
