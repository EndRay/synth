package synthesizer;

import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.DC;
import synthesizer.sources.utils.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.Collection;

import static synthesizer.sources.SignalSource.sampleRate;

public class SoundPlayer {
    private final Socket source = new Socket();
    private final Socket masterVolume = new Socket(1);
    private final SignalSource sound = source.clipBi().attenuate(masterVolume);

    private SourceDataLine sdl = null;

    public SoundPlayer() {
        this(new DC());
    }

    public SoundPlayer(SignalSource source) {
        this(source, new DC(1));
    }
    public SoundPlayer(SignalSource source, SignalSource masterVolume) {
        setSource(source);
        setMasterVolume(masterVolume);
    }

    public void setSource(SignalSource source){
        this.source.bind(source);
    }

    public void setMasterVolume(SignalSource masterVolume){
        this.masterVolume.bind(masterVolume);
    }

    private final Collection<TimeDependent> timeDependents = new ArrayList<>();

    public void addTimeDependent(TimeDependent timeDependent){
        timeDependents.add(timeDependent);
    }

    private void samplePassed(){
        for(TimeDependent timeDependent : timeDependents)
            timeDependent.samplePassed();
    }

    public synchronized void play() {
        stop();
        try {
            AudioFormat af = new AudioFormat((float) sampleRate, 16, 1, true, false);
            sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af, (int) (0.1 * sampleRate));
            sdl.start();
            new Thread(() -> {
                byte[] buf = new byte[2];
                for (int i = 0; sdl.isOpen(); i++) {
                    double sample = sound.getSample(i);
                    samplePassed();
                    int sampleInt = (int) (sample * 0x7fffffff);
                    buf[1] = (byte) ((sampleInt >> 24) & 0xff);
                    buf[0] = (byte) ((sampleInt >> 16) & 0xff);
                    sdl.write(buf, 0, 2);
                }
            }).start();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void stop(){
        if(sdl != null) {
            sdl.stop();
            sdl.close();
        }
    }
}
