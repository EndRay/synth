package sequencer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static sequencer.Clock.PPQ;

public class Sequencer implements Transmitter, Clockable{
    private Sequence sequence = null;
    private Receiver receiver;
    private final ExecutorService playing = Executors.newCachedThreadPool();
    private int midiChannel, pingsRemain = 1;
    private Long lastTimeOfPing = null;
    private boolean isPlaying = false;
    private Iterator<Step> stepIterator = null;
    private final AverageBPMCalculator averageBPMCalculator = new AverageBPMCalculator(6);

    public Sequencer(Receiver receiver, int midiChannel) {
        setReceiver(receiver);
        this.midiChannel = midiChannel;
    }

    @Override
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public void setMidiChannel(int midiChannel) {
        this.midiChannel = midiChannel;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public Receiver getReceiver() {
        return receiver;
    }

    public void play() {
        if(sequence == null)
            throw new SequencerException("Play in sequencer called without sequence provided.");
        isPlaying = true;
        if(stepIterator == null)
            stepIterator = sequence.iterator();
        pingsRemain = 1;
    }

    public void stop() {
        playing.shutdownNow();
        lastTimeOfPing = null;
        isPlaying = false;
        stepIterator = null;
    }

    public void pause(){
        lastTimeOfPing = null;
        isPlaying = false;
    }

    @Override
    public void close() {
        stop();
    }


    private final static class AverageBPMCalculator{
        private final Deque<Long> intervals = new ArrayDeque<>();
        private final int amountOfPingsToCalcBPM;
        private final static double initBPM = 120;
        private double allIntervalsSum = 0;
        AverageBPMCalculator(int amountOfPingsToCalcBPM){
            if(amountOfPingsToCalcBPM <= 0)
                throw new SequencerException("Wrong amount of pings.");
            this.amountOfPingsToCalcBPM = amountOfPingsToCalcBPM;
        }
        public double getDerivedBPM(){
            if(intervals.isEmpty())
                return initBPM;
            return (60 * 1e9) / (allIntervalsSum / intervals.size() * PPQ);
        }
        public void addInterval(Long interval){
            allIntervalsSum += interval;
            intervals.addLast(interval);
            if(intervals.size() > amountOfPingsToCalcBPM)
                //noinspection ConstantConditions
                allIntervalsSum -= intervals.pollFirst();
        }
    }

    @Override
    public void ping() {
        if(!isPlaying)
            return;
        --pingsRemain;
        Long nowTime = System.nanoTime();
        if(lastTimeOfPing != null)
            averageBPMCalculator.addInterval(nowTime - lastTimeOfPing);
        lastTimeOfPing = nowTime;
        if(pingsRemain == 0){
            playStep();
            pingsRemain = sequence.getMeasureDivision().getPings();
        }
    }

    void playStep(){
        Step nowStep;
        if(!stepIterator.hasNext())
            stepIterator = sequence.iterator();
        nowStep = stepIterator.next();
        List<Note> notes = nowStep.getNotes();
        double playLengthInMilliseconds = (60 * 1000) / (averageBPMCalculator.getDerivedBPM() * sequence.getMeasureDivision().getDivision());
        for(Note note : notes){
            try {
                receiver.send(new ShortMessage(ShortMessage.NOTE_ON, midiChannel, note.pitch(), note.velocity()), 0);
                playing.submit(() -> {
                    try {
                        TimeUnit.MILLISECONDS.sleep((long) (note.gate() * playLengthInMilliseconds));
                        receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, midiChannel, note.pitch(), 0), 0);
                    } catch (InterruptedException ignored) {}
                    catch (InvalidMidiDataException e) {
                        throw new SequencerException(e);
                    }
                });
            } catch (InvalidMidiDataException e) {
                throw new SequencerException(e);
            }
        }
    }

}
