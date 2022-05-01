package ui.structscript;

import java.util.ArrayList;
import java.util.List;

import static synthesizer.sources.SignalSource.*;
import static ui.structscript.Lexer.TokenType.*;

/**
 * StructScript Lexer! :,)
 */

public class Lexer {
    private final String code;

    public Lexer(String code) {
        this.code = code;
    }

    public enum TokenType {
        OPEN_BRACKET,
        CLOSE_BRACKET,
        DOT,
        COMMA,

        NEW,

        END_OF_LINE,
        END_OF_CODE,

        OPERATOR,

        MODE_CHANGE,
        MODE_ONCE,

        NUMBER,
        TEXT,
        FIELD,
    }

    public record Token(int line, TokenType type, Object info) {
        public Token(int line, TokenType type) {
            this(line, type, null);
        }

        @Override
        public String toString() {
            if (info == null)
                return "Token{" +
                        "type=" + type +
                        '}';
            return "Token{" +
                    "type=" + type +
                    ", info=" + info +
                    '}';
        }
    }

    private static double parseNumber(String arg) throws NumberFormatException {
        arg = arg.trim();
        if (arg.endsWith("hz"))
            return frequencyToVoltage(Double.parseDouble(arg.substring(0, arg.length() - 2)));
        if (arg.endsWith("ms"))
            return timeToVoltage(Double.parseDouble(arg.substring(0, arg.length() - 2)) / 1000);
        if (arg.endsWith("s"))
            return timeToVoltage(Double.parseDouble(arg.substring(0, arg.length() - 1)));
        if (arg.endsWith("x"))
            return frequencyRatioToVoltage(Double.parseDouble(arg.substring(0, arg.length() - 1)));
        return Double.parseDouble(arg);
    }

    private static boolean isWordCharacter(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_';
    }


    private static boolean isNumberCharacter(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '.';
    }

    private static String consumeFirstToken(int lineId, String line, List<Token> res) throws LexicalException {
        line = line.trim();
        if (line.isBlank())
            return "";
// comment
        if (line.startsWith("#")) {
            return "";
        }
        if (line.startsWith("-v-")) {
            res.add(new Token(lineId, MODE_CHANGE, "v"));
            return line.substring(3);
        }
        if (line.startsWith("---")) {
            res.add(new Token(lineId, MODE_CHANGE, "g"));
            return line.substring(3);
        }
        if (line.startsWith("v:")) {
            res.add(new Token(lineId, MODE_ONCE, "v"));
            return line.substring(2);
        }
        if (line.startsWith("g:")) {
            res.add(new Token(lineId, MODE_ONCE, "g"));
            return line.substring(2);
        }
        if (line.startsWith("(")) {
            res.add(new Token(lineId, OPEN_BRACKET));
            return line.substring(1);
        }
        if (line.startsWith(")")) {
            res.add(new Token(lineId, CLOSE_BRACKET));
            return line.substring(1);
        }
        if (line.startsWith(".")) {
            res.add(new Token(lineId, DOT));
            return line.substring(1);
        }
        if (line.startsWith(",")) {
            res.add(new Token(lineId, COMMA));
            return line.substring(1);
        }
        if (line.startsWith("\"")) {
            int pos = 1;
            while (pos < line.length() && line.charAt(pos) != '\"')
                ++pos;
            if (pos == line.length())
                throw new LexicalException(lineId, "quotes are not closed");
            res.add(new Token(lineId, TEXT, line.substring(1, pos)));
            return line.substring(pos + 1);
        }
        if (line.startsWith("new ")) {
            res.add(new Token(lineId, NEW));
            return line.substring(4);
        }
        if (line.startsWith("->-")) {
            res.add(new Token(lineId, OPERATOR, "->-"));
            return line.substring(3);
        }
        if (line.startsWith("->")) {
            res.add(new Token(lineId, OPERATOR, "->"));
            return line.substring(2);
        }
        if (line.startsWith("=>")) {
            res.add(new Token(lineId, OPERATOR, "=>"));
            return line.substring(2);
        }
        if (line.startsWith("-<-")) {
            res.add(new Token(lineId, OPERATOR, "-<-"));
            return line.substring(3);
        }
        if (line.startsWith("<-")) {
            res.add(new Token(lineId, OPERATOR, "<-"));
            return line.substring(2);
        }
        if (line.startsWith("<=")) {
            res.add(new Token(lineId, OPERATOR, "<="));
            return line.substring(2);
        }

        if (line.startsWith("=")) {
            res.add(new Token(lineId, OPERATOR, "="));
            return line.substring(1);
        }
        if (line.startsWith(":=")) {
            res.add(new Token(lineId, OPERATOR, ":="));
            return line.substring(2);
        }

        if (line.startsWith("+") || line.startsWith("-") || line.startsWith("*") || line.startsWith("/")) {
            res.add(new Token(lineId, OPERATOR, line.substring(0, 1)));
            return line.substring(1);
        }



        if (Character.isDigit(line.charAt(0))) {
            int pos = 0;
            while (pos < line.length() && isNumberCharacter(line.charAt(pos)))
                ++pos;
            try {
                double num = parseNumber(line.substring(0, pos));
                res.add(new Token(lineId, NUMBER, num));
                return line.substring(pos);
            } catch (NumberFormatException ignore) {
            }
        }
        int pos = 0;
        while (pos < line.length() && isWordCharacter(line.charAt(pos)))
            ++pos;
        if (pos == 0)
            throw new LexicalException(lineId, line);
        res.add(new Token(lineId, FIELD, line.substring(0, pos)));
        return line.substring(pos);
    }


    public List<Token> lex() throws LexicalException {
        int lineId = 1;
        List<Token> res = new ArrayList<>();
        for (String line : code.split("\\n")) {
            while (!line.isBlank())
                line = consumeFirstToken(lineId, line, res);
            res.add(new Token(lineId, END_OF_LINE));
            ++lineId;
        }
        res.add(new Token(lineId, END_OF_CODE));
        return res;
    }
}
