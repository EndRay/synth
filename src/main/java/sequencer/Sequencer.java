package sequencer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static sequencer.Clock.PPQ;

public class Sequencer implements Transmitter, Clockable {
    private Sequence sequence = null;
    private Receiver receiver;
    private final Collection<Thread> playing = new ArrayList<>();
    private int midiChannel, pingsRemain = 1;
    private Long lastTimeOfPing = null;
    private boolean isPlaying = false;
    private boolean isMuted = false;
    private int stepIndex = 0;
    private final AverageBPMCalculator averageBPMCalculator = new AverageBPMCalculator(6);

    public Sequencer(Receiver receiver, int midiChannel) {
        setReceiver(receiver);
        this.midiChannel = midiChannel;
    }

    @Override
    public synchronized void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public synchronized void setMidiChannel(int midiChannel) {
        this.midiChannel = midiChannel;
    }

    public synchronized void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public synchronized void setMuted(boolean isMuted){
        this.isMuted = isMuted;
    }
    public synchronized boolean isMuted(){
        return isMuted;
    }
    public synchronized void mute(){
        setMuted(true);
    }

    public synchronized void unmute(){
        setMuted(false);
    }



    @Override
    public synchronized Receiver getReceiver() {
        return receiver;
    }

    public synchronized void play() {
        if(isPlaying)
            return;
        if (sequence == null)
            return;
        isPlaying = true;
        stepIndex = 0;
        pingsRemain = 1;
    }

    public synchronized void stop() {
        for(Thread thread : playing)
            thread.interrupt();
        playing.clear();
        lastTimeOfPing = null;
        isPlaying = false;
    }

    public synchronized void pause() {
        lastTimeOfPing = null;
        isPlaying = false;
    }

    @Override
    public void close() {
        stop();
    }


    private final static class AverageBPMCalculator {
        private final Deque<Long> intervals = new ArrayDeque<>();
        private final int amountOfPingsToCalcBPM;
        private final static double initBPM = 120;
        private double allIntervalsSum = 0;

        AverageBPMCalculator(int amountOfPingsToCalcBPM) {
            if (amountOfPingsToCalcBPM <= 0)
                throw new SequencerException("Wrong amount of pings.");
            this.amountOfPingsToCalcBPM = amountOfPingsToCalcBPM;
        }

        public double getDerivedBPM() {
            if (intervals.isEmpty())
                return initBPM;
            return (60 * 1e9) / (allIntervalsSum / intervals.size() * PPQ);
        }

        public void addInterval(Long interval) {
            allIntervalsSum += interval;
            intervals.addLast(interval);
            if (intervals.size() > amountOfPingsToCalcBPM)
                //noinspection ConstantConditions
                allIntervalsSum -= intervals.pollFirst();
        }
    }

    @Override
    public synchronized void ping() {
        if (!isPlaying)
            return;
        --pingsRemain;
        Long nowTime = System.nanoTime();
        if (lastTimeOfPing != null)
            averageBPMCalculator.addInterval(nowTime - lastTimeOfPing);
        lastTimeOfPing = nowTime;
        if (pingsRemain == 0) {
            playNextStep();
            pingsRemain = sequence.getMeasureDivision().getPings();
        }
    }

    @Override
    public void start() {
        stop();
        play();
    }

    void playStep(Step step){
        int channel = midiChannel;
        if(channel == -1 || isMuted())
            return;
        double playLengthInMilliseconds = (60 * 1000) / (averageBPMCalculator.getDerivedBPM() * sequence.getMeasureDivision().getDivision());
        for (Note note : step.getNotes()) {
            try {
                receiver.send(new ShortMessage(ShortMessage.NOTE_ON, channel, note.getPitch(), note.getVelocity()), 0);
                Thread thread = new Thread(() -> {
                    try {
                        try {
                            TimeUnit.MILLISECONDS.sleep((long) (note.getGate() * playLengthInMilliseconds));
                        } catch (InterruptedException ignored) {
                        } finally {
                            receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, channel, note.getPitch(), 0), 0);
                        }
                    } catch (InvalidMidiDataException e) {
                        throw new SequencerException(e);
                    }
                });
                thread.setDaemon(true);
                thread.start();
                playing.add(thread);
            } catch (InvalidMidiDataException e) {
                throw new SequencerException(e);
            }
        }
    }

    void playNextStep() {
        if(sequence.length() == 0)
            return;
        ++stepIndex;
        while(stepIndex >= sequence.length())
            stepIndex -= sequence.length();
        playStep(sequence.getStep(stepIndex));
    }

}
