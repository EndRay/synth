package ui.structscript;

public class LexicalException extends StructScriptException{
    public LexicalException(int line, String message) {
        super(line, message);
    }

    @Override
    public String toString() {
        return "lexical error in line " + getLine() + ": " + getMessage();
    }
}
