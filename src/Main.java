import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
    final static int sampleRate = 44100;

    static void play(SynthBuilder builder) throws LineUnavailableException, IOException {


        Synth synth = builder.getSynth();

        SynthMidiReceiver midiReceiver = new SynthMidiReceiver(synth);
        MidiDevice device;
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < infos.length; i++) {
            try {
                device = MidiSystem.getMidiDevice(infos[i]);
                List<Transmitter> transmitters = device.getTransmitters();
                for (Transmitter transmitter : transmitters) {
                    transmitter.setReceiver(midiReceiver);
                }

                Transmitter trans = device.getTransmitter();
                trans.setReceiver(midiReceiver);
                device.open();
                //System.out.println(device.getDeviceInfo()+" Was Opened");


            } catch (MidiUnavailableException e) {
                //System.out.println("Device " + i + " error");
            }
        }
        byte[] buf = new byte[2];
        AudioFormat af = new AudioFormat((float) sampleRate, 16, 1, true, false);
        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af, (int) (0.1 * sampleRate));
        sdl.start();
        //int samples = (int) (200 * (float) sampleRate);

        Scanner console = new Scanner(System.in);

        Synth output = builder.getSynth();//new Socket(synth);

        for (int i = 0; true; i++) {
            if (i % 100 == 0 && System.in.available() > 0) {
                String line = console.nextLine();
                if(line.equals("quit"))
                    break;
//                if ("noteOn".equals(line)) {
//                    synth.noteOn(60);
//                }
//                if ("noteOff".equals(line)) {
//                    synth.noteOff(60);
//                }
                builder.handleCommand(line);
            }
            double sample = output.getSample(i);
            int sampleInt = (int) (sample * 0x7fffffff);
            buf[1] = (byte) ((sampleInt >> 24) & 0xff);
            buf[0] = (byte) ((sampleInt >> 16) & 0xff);
            sdl.write(buf, 0, 2);
        }
        sdl.drain();
        sdl.stop();
    }

    public static void main(String[] args) throws LineUnavailableException, IOException {
        SynthBuilder builder = new SynthBuilder(6);
        if(args.length > 0){
            try{
                File basic = new File(args[0]);
                Scanner reader = new Scanner(basic);
                while (reader.hasNextLine())
                    builder.handleCommand(reader.nextLine());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        play(builder);
    }
}
