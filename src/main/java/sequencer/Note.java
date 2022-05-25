package sequencer;

public class Note{
    private int pitch;
    private int velocity;
    private double gate;

    public Note(int pitch, int velocity, double gate) {
        this.setPitch(pitch);
        this.setVelocity(velocity);
        this.setGate(gate);
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        if (pitch < 0 || pitch > 127)
            throw new SequenceException("Invalid note pitch.");
        this.pitch = pitch;
    }

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        if (velocity < 1 || velocity > 127)
            throw new SequenceException("Invalid note velocity.");
        this.velocity = velocity;
    }

    public double getGate() {
        return gate;
    }

    public void setGate(double gate) {
        if (gate < 0)
            throw new SequenceException("Invalid note gate.");
        this.gate = gate;
    }
}
