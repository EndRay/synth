package sequencer;

public class SequencerException extends RuntimeException{
    public SequencerException() {}
    public SequencerException(String message){
        super(message);
    }
    public SequencerException(Exception e){
        super(e);
    }
}
