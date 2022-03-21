import Sources.Filters.Filter;
import Sources.Filters.LowPass1PoleFilter;
import Sources.Filters.ResonantLowPass2PoleFilter;
import Sources.Oscillators.Oscillator;
import Sources.Oscillators.SawOscillator;
import Sources.Oscillators.SineOscillator;
import Sources.SignalSource;
import Sources.Utils.Attenuator;
import Sources.Utils.DC;
import Sources.Utils.FrequencyAdder;
import Sources.Utils.Mixer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Synth {
    final static int sampleRate = 44100;

    static class Output {
        SignalSource source;

        Output(SignalSource source) {
            this.source = source;
        }

        void play(int seconds) throws LineUnavailableException {
            byte[] buf = new byte[2];
            AudioFormat af = new AudioFormat((float) sampleRate, 16, 1, true, false);
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            sdl.open();
            sdl.start();
            int samples = (int) (seconds * (float) sampleRate);
            for (int i = 0; i < samples; i++) {
                double sample = source.getSample(i);
                int sampleInt = (int) (sample * 0x7fffffff);
//                System.out.println(sample);
//                System.out.println(sampleInt);
//                System.out.println((sampleInt >> 24) & 0xff);
//                System.out.println((sampleInt >> 16) & 0xff);
                buf[1] = (byte) ((sampleInt >> 24) & 0xff);
                buf[0] = (byte) ((sampleInt >> 16) & 0xff);
                sdl.write(buf, 0, 2);
            }
            sdl.drain();
            sdl.stop();
        }
    }

//    public static void main(String[] args) throws LineUnavailableException {
//        Oscillator osc = new SawOscillator(DC.getFrequencyDC(110));
//        Oscillator osc2 = new SawOscillator(DC.getFrequencyDC(110.5));
//        Oscillator osc3 = new SawOscillator(DC.getFrequencyDC(110.25));
//        Oscillator sub = new SineOscillator(DC.getFrequencyDC(55));
//        Mixer mixer = new Mixer(osc, osc2, osc3, sub);
//        SignalSource modLFO = new SineOscillator(DC.getFrequencyDC(0.5));
//        modLFO = new Attenuator(modLFO, 0.05);
//        modLFO = new Mixer(DC.getFrequencyDC(6), modLFO);
//        SignalSource LFO = new SawOscillator(modLFO);
//        SignalSource resonant_cutoff = new Mixer(new Attenuator(LFO, -0.1), DC.getFrequencyDC(1300));
//        SignalSource filter = new LowPass1PoleFilter(mixer, DC.getFrequencyDC(110));
//        SignalSource resonant = new ResonantLowPass2PoleFilter(mixer, resonant_cutoff, new DC(0.8));
//        SignalSource result = new Attenuator(new Mixer(filter, resonant), 0.1);
//
//        Output output = new Output(result);
//        output.play(20);
//    }
    public static void main(String[] args) throws LineUnavailableException {
        DC frequency = DC.getFrequencyDC(110);
        Oscillator osc = new SawOscillator(frequency);
        Oscillator osc2 = new SawOscillator(new FrequencyAdder(frequency, DC.getFrequencyDC(1)));
        frequency.setFrequency(220);

        SignalSource result = new Attenuator(new Mixer(osc, osc2), 0.2);

        Output output = new Output(result);
        output.play(20);
    }
}
