import realisations.modulators.SimpleADSREnvelope;
import realisations.oscillators.PMSineOscillator;
import realisations.oscillators.SineOscillator;
import realisations.oscillators.TriangleOscillator;
import sources.Gated;
import sources.SignalSource;
import sources.Triggerable;
import sources.modulators.Envelope;
import sources.utils.*;
import sources.voices.Voice;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.List;

import static sources.SignalSource.frequencyToVoltage;
import static sources.SignalSource.voltageToFrequency;

public class Main {
    final static int sampleRate = 44100;

    static void play(Synth synth) throws LineUnavailableException {

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
        int samples = (int) (200 * (float) sampleRate);
        for (int i = 0; i < samples; i++) {
            double sample = synth.getSample(i);
            int sampleInt = (int) (sample * 0x7fffffff);
            buf[1] = (byte) ((sampleInt >> 24) & 0xff);
            buf[0] = (byte) ((sampleInt >> 16) & 0xff);
            sdl.write(buf, 0, 2);
        }
        sdl.drain();
        sdl.stop();
    }

    public static void main(String[] args) throws LineUnavailableException {
        final int voicesCount = 1;

        SourceValue unusedSourceValue = new SourceValue("Unused value");

        Voice[] voices = new Voice[voicesCount];
        SourceValue[] CCValues = {};

        for (int i = 0; i < voicesCount; ++i) {
            SourceValue frequency = new SourceValue("note frequency", frequencyToVoltage(220));
            SourceValue velocity = new SourceValue("note velocity");
            SignalSource modulator = new SineOscillator(frequency.multiplyFrequency(3));
            Envelope env = new SimpleADSREnvelope(0, 5, 0, 5);
            SignalSource carrier = new PMSineOscillator(frequency, modulator.attenuate(env).attenuate(0.3));
            SignalSource result = carrier.attenuate(0.2);
            Gated multiGate = new MultiGate(env);
            Triggerable multiTrigger = new MultiTrigger();

            Voice voice = new Voice(result, unusedSourceValue, velocity, unusedSourceValue, unusedSourceValue, multiGate, multiTrigger);
            voices[i] = voice;

            multiGate.gateOn();
        }


        //Synth synth = new MyMonoSynth(result, frequency, new MultiGate(env, filterEnv));
        Synth synth = new MyPolySynth(voices, new Mixer(voices).clipBi(), CCValues);
        play(synth);
    }
}
