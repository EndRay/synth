import Sources.Filters.Filter;
import Sources.Filters.LowPass1PoleFilter;
import Sources.Filters.ResonantLowPass2PoleFilter;
import Sources.Oscillators.Oscillator;
import Sources.Oscillators.SawOscillator;
import Sources.Oscillators.SineOscillator;
import Sources.SignalSource;
import Sources.Utils.Attenuator;
import Sources.Utils.DC;
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

    public static void main(String[] args) throws LineUnavailableException {
        Oscillator osc = new SawOscillator(110);
        Oscillator osc2 = new SawOscillator(110.5);
        Oscillator osc3 = new SawOscillator(110.25);
        Oscillator sub = new SineOscillator(55);
        Mixer mixer = new Mixer(osc, osc2, osc3, sub);
        Filter filter = new LowPass1PoleFilter(mixer, 110);
        SignalSource resonant = new ResonantLowPass2PoleFilter(mixer, 1600, 0.8);
        SignalSource result = new Attenuator(new Mixer(filter, resonant), 0.1);

        Output output = new Output(result);
        output.play(6);
    }
}
