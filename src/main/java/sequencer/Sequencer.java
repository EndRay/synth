package sequencer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class Sequencer implements Transmitter {
    Receiver receiver;
    List<Integer> notes = new ArrayList<>();
    Thread player;
    int BPM = 270, midiChannel;

    public Sequencer(Receiver receiver, int midiChannel) {
        setReceiver(receiver);
        this.midiChannel = midiChannel;
    }

    @Override
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public Receiver getReceiver() {
        return receiver;
    }

    private void setNotes(List<Integer> notes) {
        this.notes = notes;
    }

    public void setBPM(int BPM) {
        this.BPM = BPM;
    }

    public void setMidiChannel(int midiChannel) {
        this.midiChannel = midiChannel;
    }

    public void play(List<Integer> notes) {
        stop();
        setNotes(notes);
        player = new Thread(() -> {
            try {
                int nowPlayingNote = -1;
                try {
                    while (true) {
                        for (Integer note : notes) {
                            nowPlayingNote = note;
                            if(nowPlayingNote != -1)
                                receiver.send(new ShortMessage(ShortMessage.NOTE_ON, midiChannel, nowPlayingNote, 64), 0);
                            TimeUnit.MILLISECONDS.sleep(1000 * 60 / BPM);
                            if(nowPlayingNote != -1)
                                receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, midiChannel, nowPlayingNote, 0), 0);
                        }
                    }
                } catch (InterruptedException e) {
                    if(nowPlayingNote != -1)
                        receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, midiChannel, nowPlayingNote, 0), 0);
                }
            } catch (InvalidMidiDataException e) {
                // ???
                e.printStackTrace();
            }
        });
        player.start();
    }

    public void stop() {
        if (player == null)
            return;
        player.interrupt();
        player = null;
    }

    @Override
    public void close() {

    }
}
