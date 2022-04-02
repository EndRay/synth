import synths.Synth;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;

/**
 * TODO: Parse polyaftertouch
 */

public class SynthMidiReceiver implements Receiver {

    Synth[][] synths;

    SynthMidiReceiver(Synth[][] synths) {
        this.synths = synths;
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        //System.out.println("I am SynthMidiReceiver and I got this message: " + Arrays.toString(message.getMessage()));
        byte[] mArr = message.getMessage();
        byte channel = (byte) (mArr[0]&((1<<4)-1)),
             action = (byte) (mArr[0]>>4);
        switch (action) {
            case -5:
                for(Synth synth : synths[channel])
                    synth.midiCC(mArr[1], mArr[2]);
                break;
            case -7:
                for (Synth synth : synths[channel])
                    synth.noteOn(mArr[1], mArr[2]);
                break;
            case -8:
                for (Synth synth : synths[channel])
                    synth.noteOff(mArr[1], mArr[2]);
                break;
        }
    }

    @Override
    public void close() {
        System.out.println("MidiReceiver closing");
    }
}
