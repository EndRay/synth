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
        System.out.println("I am SynthMidiReceiver and I got this message: " + Arrays.toString(message.getMessage()));
        switch (message.getMessage()[0]) {
            case -112:
                for (Synth synth : synths)
                    synth.noteOn(message.getMessage()[1], message.getMessage()[2]);
                break;
            case -128:
                for (Synth synth : synths)
                    synth.noteOff(message.getMessage()[1], message.getMessage()[2]);
                break;
        }
    }

    @Override
    public void close() {
        System.out.println("MidiReceiver closing");
    }
}
