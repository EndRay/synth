import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import java.util.Arrays;

public class SynthMidiReceiver implements Receiver {

    Synth[] synths;

    SynthMidiReceiver(Synth synth) {
        synths = new Synth[1];
        synths[0] = synth;
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        //System.out.println("I am SynthMidiReceiver and I got this message: " + Arrays.toString(message.getMessage()));
        byte[] mArr = message.getMessage();
        switch (mArr[0]) {
            case -80:
                for(Synth synth : synths)
                    synth.midiCC(mArr[1], mArr[2]);
                break;
            case -112:
                for (Synth synth : synths)
                    synth.noteOn(mArr[1], mArr[2]);
                break;
            case -128:
                for (Synth synth : synths)
                    synth.noteOff(mArr[1], mArr[2]);
                break;
        }
    }

    @Override
    public void close() {
        System.out.println("MidiReceiver closing");
    }
}
