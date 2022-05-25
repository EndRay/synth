package sequencer;

public class Note{
    private int pitch;
    private Integer velocity;
    private Double gate;

    public Note(Integer pitch, Integer velocity, Double gate) {
        this.setPitch(pitch);
        this.setVelocity(velocity);
        this.setGate(gate);
    }

    public Integer getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        if (pitch < 0 || pitch > 127)
            throw new SequenceException("Invalid note pitch.");
        this.pitch = pitch;
    }

    public Integer getVelocity() {
        return velocity;
    }

    public void setVelocity(Integer velocity) {
        if (velocity != null && (velocity < 1 || velocity > 127))
            throw new SequenceException("Invalid note velocity.");
        this.velocity = velocity;
    }

    public Double getGate() {
        return gate;
    }

    public void setGate(Double gate) {
        if (gate != null && gate < 0)
            throw new SequenceException("Invalid note gate.");
        this.gate = gate;
    }
}
