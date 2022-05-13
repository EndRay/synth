package sequencer;

public record Note(int pitch, int velocity, double gate) {
    public Note {
        if (pitch < 0 || pitch > 127)
            throw new SequenceException("Invalid note pitch.");
        if (gate < 0)
            throw new SequenceException("Invalid note gate.");
        if (velocity < 1 || velocity > 127)
            throw new SequenceException("Invalid note velocity.");
    }
}
