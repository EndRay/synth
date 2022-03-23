import sources.Gated;
import sources.SignalSource;
import sources.Triggerable;
import sources.filters.ResonantLowPass2PoleFilter;
import sources.modulators.SimpleADSREnvelope;
import sources.modulators.Envelope;
import sources.oscillators.Oscillator;
import sources.oscillators.SawOscillator;
import sources.oscillators.SineOscillator;
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

import static sources.SignalSource.frequencyCoefficientToVoltage;
import static sources.SignalSource.frequencyToVoltage;

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
                System.out.println(device.getDeviceInfo()+" Was Opened");


            } catch (MidiUnavailableException e) {
                System.out.println("Device " + i + " error");
            }
        }
        byte[] buf = new byte[2];
        AudioFormat af = new AudioFormat((float) sampleRate, 16, 1, true, false);
        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af, (int)(0.1*sampleRate));
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
        final int voicesCount = 6;

        SourceValue unusedSourceValue = new SourceValue("Unused value");

        Voice[] voices = new Voice[voicesCount];

        SignalSource LFO = new SineOscillator(DC.getFrequencyDC(0.2));

        for(int i = 0; i < voicesCount; ++i) {
            SourceValue frequency = new SourceValue("note frequency", frequencyToVoltage(110));
            SourceValue velocity = new SourceValue("note velocity");
            Oscillator osc = new SawOscillator(frequency);
            Oscillator osc2 = new SawOscillator(new FrequencyAdder(frequency, DC.getFrequencyDC(1)));
            Mixer mixer = new Mixer(osc, osc2);
            Envelope env = new SimpleADSREnvelope(2, 5, 4);
            Envelope filterEnv = new SimpleADSREnvelope(3, 4);
            SignalSource filter = new ResonantLowPass2PoleFilter(mixer, filterEnv.attenuated(velocity.map(0.5, 1)).map(frequencyToVoltage(600), frequencyToVoltage(4000)).add(LFO.map(frequencyCoefficientToVoltage(0.8), frequencyCoefficientToVoltage(1.2))), new DC(0.7));
            SignalSource result = filter.attenuated(env).attenuated(0.2);
            Gated multiGate = new MultiGate(env, filterEnv);
            Triggerable multiTrigger = new MultiTrigger();

            Voice voice = new Voice(result, frequency, velocity, unusedSourceValue, unusedSourceValue, multiGate, multiTrigger);
            voices[i] = voice;
        }

        //Synth synth = new MyMonoSynth(result, frequency, new MultiGate(env, filterEnv));
        Synth synth = new MyPolySynth(voices);
        play(synth);
    }
}
