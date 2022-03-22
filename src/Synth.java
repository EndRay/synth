import Sources.Filters.LowPass1PoleFilter;
import Sources.Filters.ResonantLowPass2PoleFilter;
import Sources.Oscillators.Oscillator;
import Sources.Oscillators.SawOscillator;
import Sources.Oscillators.SineOscillator;
import Sources.SignalSource;
import Sources.Utils.*;

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
                buf[1] = (byte) ((sampleInt >> 24) & 0xff);
                buf[0] = (byte) ((sampleInt >> 16) & 0xff);
                sdl.write(buf, 0, 2);
            }
            sdl.drain();
            sdl.stop();
        }
    }

    static SignalSource getSineOscillator(SignalSource frequencySource, SignalSource min, SignalSource max){
        SignalSource osc = new SineOscillator(frequencySource);
        SignalSource dif = new UnityMixer(max, min.attenuated(-1));
        SignalSource avg = new UnityMixer(max, min);
        osc = new Mixer(avg, osc.attenuated(dif));
        return osc;
    }

    public static void main(String[] args) throws LineUnavailableException {
//        Oscillator osc = new SawOscillator(DC.getFrequencyDC(110));
//        Oscillator osc2 = new SawOscillator(DC.getFrequencyDC(110.5));
//        Oscillator osc3 = new SawOscillator(DC.getFrequencyDC(110.25));
//        Oscillator sub = new SineOscillator(DC.getFrequencyDC(55));
//        Mixer mixer = new Mixer(osc, osc2, osc3, sub);
//        SignalSource modLFO = getSineOscillator(DC.getFrequencyDC(0.5), DC.getFrequencyDC(3), DC.getFrequencyDC(12));
//        SignalSource LFO = new SawOscillator(modLFO);
//        SignalSource resonant_cutoff = new Mixer(new Attenuator(LFO, new DC(-0.1)), DC.getFrequencyDC(1300));
//        SignalSource filter = new LowPass1PoleFilter(mixer, DC.getFrequencyDC(110));
//        SignalSource resonant = new ResonantLowPass2PoleFilter(mixer, resonant_cutoff, new DC(0.8));
//        SignalSource result = new Attenuator(new UnityMixer(filter, resonant), new DC(0.2));

        DC frequency = DC.getFrequencyDC(110);
        Oscillator osc = new SawOscillator(frequency);
        SignalSource vibratoPercent = DC.getFrequencyCoefficientDC(2);
        SignalSource LFO = getSineOscillator(DC.getFrequencyDC(0.1), new Mixer(frequency, vibratoPercent), new Mixer(frequency, vibratoPercent.attenuated(-1)));
        osc.setFrequency(LFO);
        Oscillator osc2 = new SawOscillator(new FrequencyAdder(frequency, DC.getFrequencyDC(1)));
        osc = new ResonantLowPass2PoleFilter(osc, DC.getFrequencyDC(2000));
        frequency.setFrequency(220);
        SignalSource osc3 = new AC();
        SignalSource result = new UnityMixer(osc, osc2, osc3, osc3, osc3, osc3).attenuated(0.2);

        Output output = new Output(result);
        output.play(20);
    }
}
