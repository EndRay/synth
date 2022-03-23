import sources.SignalSource;
import sources.filters.ResonantLowPass2PoleFilter;
import sources.modulators.SimpleADSREnvelope;
import sources.modulators.Envelope;
import sources.oscillators.Oscillator;
import sources.oscillators.SawOscillator;
import sources.oscillators.SineOscillator;
import sources.utils.*;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.List;

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
                System.out.println("wtf bro");
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

    static SignalSource getSineOscillator(SignalSource frequencySource, SignalSource min, SignalSource max){
        SignalSource osc = new SineOscillator(frequencySource);
        SignalSource dif = new UnityMixer(max, min.attenuated(-1));
        SignalSource avg = new UnityMixer(max, min);
        osc = new Mixer(avg, osc.attenuated(dif));
        return osc;
    }

    public static void main(String[] args) throws LineUnavailableException {
        SourceValue frequency = new SourceValue("note frequency", SignalSource.frequencyToVoltage(110));
        Oscillator osc = new SawOscillator(frequency);
        Oscillator osc2 = new SawOscillator(new FrequencyAdder(frequency, DC.getFrequencyDC(0.25)));
        Oscillator osc3 = new SawOscillator(new FrequencyAdder(frequency, DC.getFrequencyDC(0.5)));
        Oscillator sub = new SineOscillator(new Mixer(frequency, DC.getFrequencyCoefficientDC(0.5)));
        Mixer mixer = new Mixer(osc, osc2, osc3, sub);
        Envelope env = new SimpleADSREnvelope(0.01, 2, 0.4, 0.1, true, false);
//        Envelope filterEnv = new SimpleADSREnvelope(1);
        SignalSource filter = new ResonantLowPass2PoleFilter(mixer, DC.getFrequencyDC(1000), new DC(0.7));
        SignalSource result = filter.attenuated(env).attenuated(0.2);



//        DC frequency = DC.getFrequencyDC(110);
//        Oscillator osc = new SawOscillator(frequency);
//        SignalSource vibratoPercent = DC.getFrequencyCoefficientDC(2);
//        SignalSource LFO = getSineOscillator(DC.getFrequencyDC(0.1), new Mixer(frequency, vibratoPercent), new Mixer(frequency, vibratoPercent.attenuated(-1)));
//        osc.setFrequency(LFO);
//        Oscillator osc2 = new SawOscillator(new FrequencyAdder(frequency, DC.getFrequencyDC(1)));
//        osc = new ResonantLowPass2PoleFilter(osc, DC.getFrequencyDC(2000));
//        frequency.setFrequency(220);
//        SignalSource osc3 = new DC();
//        SignalSource result = new UnityMixer(osc, osc2, osc3, osc3, osc3, osc3).attenuated(0.2);


        Synth synth = new MyMonoSynth(result, frequency, env);
        play(synth);
    }
}
