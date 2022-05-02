package sequencer;

public class SequenceException extends RuntimeException{
    public SequenceException() {}
    public SequenceException(String message){
        super(message);
    }
    public SequenceException(Exception e){
        super(e);
    }
}
