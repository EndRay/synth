package structscript;

public class LexicalException extends StructScriptException{
    public LexicalException(int line, String message) {
        super(line, message);
    }

    @Override
    public String getStructScriptMessage(){
        return "lexical error in line " + getLine() + ": " + getMessage();
    }
}
