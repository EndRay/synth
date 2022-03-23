import realisations.filters.ResonantLowPass2PoleFilter;
import realisations.modulators.SimpleADSREnvelope;
import realisations.oscillators.SawOscillator;
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
        final int voicesCount = 6;

        SourceValue unusedSourceValue = new SourceValue("Unused value");

        SourceValue filterCutoff = new SourceValue("Filter Cutoff", 0.3);
        SourceValue filterResonance = new SourceValue("Filter Resonance", 0.2);
        SourceValue filterKeytrack = new SourceValue("Filter Keytrack", 0.8);
        SourceValue filterEnvAmount = new SourceValue("Filter Envelope Amount", 0.7);

        SourceValue mix1 = new SourceValue("Oscillator #1 Volume", 0.25);
        SourceValue mix2 = new SourceValue("Oscillator #2 Volume", 0.25);
        SourceValue mix3 = new SourceValue("Oscillator #3 Volume", 0.25);
        SourceValue mix4 = new SourceValue("Oscillator #4 Volume", 0.25);

        Voice[] voices = new Voice[voicesCount];
        SourceValue[] CCValues = {filterCutoff, filterResonance, filterKeytrack, filterEnvAmount, mix1, mix2, mix3, mix4};

        for (int i = 0; i < voicesCount; ++i) {
            SourceValue frequency = new SourceValue("note frequency", frequencyToVoltage(110));
            SourceValue velocity = new SourceValue("note velocity");
            SignalSource osc = new SawOscillator(frequency, true);
            SignalSource osc2 = new SawOscillator(frequency.add(DC.getFrequencyCoefficientDC(1.01)), true);
            SignalSource osc3 = new SawOscillator(frequency.add(DC.getFrequencyCoefficientDC(1.02)), true);
            SignalSource sub = new TriangleOscillator(frequency.add(DC.getFrequencyCoefficientDC(0.5))).attenuate(0.5);
            Mixer mixer = new Mixer(osc.attenuate(mix1), osc2.attenuate(mix2), osc3.attenuate(mix3), sub.attenuate(mix4));
            Envelope env = new SimpleADSREnvelope(0.02, 3, 0.4, 0.5);
            SignalSource filter = new ResonantLowPass2PoleFilter(mixer, filterCutoff.add(frequency.attenuate(filterKeytrack)).add(env.attenuate(filterEnvAmount.mapUni(-1, 1))).clipUni(), filterResonance);
            SignalSource result = filter.attenuate(env).attenuate(0.1);
            Gated multiGate = new MultiGate(env);
            Triggerable multiTrigger = new MultiTrigger();

            Voice voice = new Voice(result, frequency, velocity, unusedSourceValue, unusedSourceValue, multiGate, multiTrigger);
            voices[i] = voice;
        }

        //Synth synth = new MyMonoSynth(result, frequency, new MultiGate(env, filterEnv));
        Synth synth = new MyPolySynth(voices, new Mixer(voices).clipBi(), CCValues);
        play(synth);
    }
}
