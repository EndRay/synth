package synthesizer;

import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.DC;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.Thread.currentThread;
import static synthesizer.sources.SignalSource.sampleRate;

public class SoundPlayer {
    private SignalSource source;

    private Thread player = null;

    public SoundPlayer() {
        this(new DC());
    }

    public SoundPlayer(SignalSource source) {
        this.source = source;
    }

    public void setSource(SignalSource source){
        this.source = source;
    }

    private final Collection<TimeDependent> timeDependents = new ArrayList<>();

    public void addTimeDependent(TimeDependent timeDependent){
        timeDependents.add(timeDependent);
    }

    private void samplePassed(){
        for(TimeDependent timeDependent : timeDependents)
            timeDependent.samplePassed();
    }

    public void play() {
        player = new Thread(()-> {
            try {
                byte[] buf = new byte[2];
                AudioFormat af = new AudioFormat((float) sampleRate, 16, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af, (int) (0.1 * sampleRate));
                sdl.start();
                for (int i = 0; true; i++) {
                    double sample = source.getSample(i);
                    samplePassed();
                    int sampleInt = (int) (sample * 0x7fffffff);
                    buf[1] = (byte) ((sampleInt >> 24) & 0xff);
                    buf[0] = (byte) ((sampleInt >> 16) & 0xff);
                    sdl.write(buf, 0, 2);
                    if(currentThread().isInterrupted())
                        break;
                }
                sdl.drain();
                sdl.stop();
            } catch (LineUnavailableException e) {
                throw new RuntimeException(e);
            }
        });
        player.start();
    }

    public void stop(){
        if(player != null)
            player.interrupt();
    }
}
