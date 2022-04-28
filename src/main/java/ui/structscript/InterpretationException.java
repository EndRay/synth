package ui.structscript;

public class InterpretationException extends StructScriptException{
    public InterpretationException(String message){
        super(-1, message);
    }
    public InterpretationException(int line, String message) {
        super(line, message);
    }

    @Override
    public String getStructScriptMessage() {
        if (getLine() == -1)
            return "interpretation error: " + getMessage();
        return "interpretation error in line " + getLine() + ": " + getMessage();
    }
}
