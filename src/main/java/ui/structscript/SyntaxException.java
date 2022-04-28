package ui.structscript;

public class SyntaxException extends StructScriptException{
    public SyntaxException(int line, String message) {
        super(line, message);
    }

    @Override
    public String getStructScriptMessage() {
        return "syntax error in line " + getLine() + ": " + getMessage();
    }
}
