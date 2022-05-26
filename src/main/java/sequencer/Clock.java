package sequencer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Clock{
    public static final int PPQ = 24;
    private double BPM;
    private Thread process = null;
    private final Set<Clockable> signalize = new HashSet<>();
    public Clock(){
        this(120);
    }
    public Clock(double BPM){
        this.BPM = BPM;
    }
    public void setBPM(double BPM){
        this.BPM = BPM;
    }
    public double getBPM(){
        return BPM;
    }
    public void add(Clockable el){
        signalize.add(el);
    }
    public void remove(Clockable el){
        signalize.remove(el);
    }
    public void clear(){
        signalize.clear();
    }
    public void start(){
        stop();
        for(Clockable el : signalize)
            el.start();
        process = new Thread(() -> {
            try {
                while(true){
                    for(Clockable el : signalize)
                        el.ping();
                    TimeUnit.MILLISECONDS.sleep((long) (60 * 1000 / BPM / PPQ));
                }
            } catch (InterruptedException ignored) {}
        });
        process.setDaemon(true);
        process.start();
    }

    public void stop(){
        if(process == null)
            return;
        process.interrupt();
        for(Clockable el : signalize)
            el.stop();
        process = null;
    }
}
