package ui.structscript;

import ui.structscript.Lexer.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ui.structscript.Lexer.TokenType;
import static ui.structscript.Parser.NodeType.*;

/**
 * StructScript Parser! :(
 * <p>
 * value = ??? operations over numbers ???
 * <p>
 * argument = value | signal | text
 * arguments = argument | arguments comma argument
 * <p>
 * function = field open_bracket arguments close_bracket
 * constructor = new function
 * <p>
 * object = field
 * socket = object dot field
 * signal = number | object | (signal) | constructor | signal dot function | ??? operations over signals ???
 * <p>
 * command = object assign signal |
 * socket set value
 * signal socket_operation socket |
 * socket back_socket_operation signal
 * <p>
 * line = voice_mode_once command | global_mode_once command | voice_mode | global_mode
 */

public class Parser {
    private final List<Token> tokens;
    private int ptr = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public enum NodeType {
        NUMBER,
        OBJECT,
        TEXT,
        SOCKET,

        CONSTRUCTOR,
        FUNCTION,

        UNARY_MINUS,
        ARITHMETIC_OPERATOR,
        ACTION,

        MODE_CHANGE,
        MODE_ONCE,
    }

    public record Node(int line, NodeType type, Object info, List<Node> args) {
        public Node(int line, NodeType type) {
            this(line, type, null, new ArrayList<>());
        }
        public Node(int line, NodeType type, Object info, Node... args) {
            this(line, type, info, List.of(args));
        }

        public Node arg(int i){
            return args.get(i);
        }
        public String text(){
            return info != null ? info.toString() : "";
        }
    }

    private Node createNode(NodeType type){
        return new Node(getToken().line(), type);
    }

    private Node createNode(NodeType type, Object info, Node... args){
        return new Node(getToken().line(), type, info, args);
    }

    private Node createNode(NodeType type, Object info, List<Node> args){
        return new Node(getToken().line(), type, info, args);
    }

    private void movePtr() {
        ++ptr;
    }

    private Token getToken() {
        return tokens.get(ptr);
    }

    private List<Node> parseArguments() throws SyntaxException {
        if (getToken().type() != TokenType.OPEN_BRACKET)
            throw new SyntaxException(getToken().line(), "open bracket expected");
        List<Node> res = new ArrayList<>();
        movePtr();
        if(getToken().type() != TokenType.CLOSE_BRACKET){
            res.add(parseExpression());
            while (getToken().type() == TokenType.COMMA){
                movePtr();
                res.add(parseExpression());
            }
        }
        if (getToken().type() != TokenType.CLOSE_BRACKET)
            throw new SyntaxException(getToken().line(), "close bracket expected");
        movePtr();
        return res;
    }

    private Node parseAtom() throws SyntaxException {
        if (ptr == tokens.size())
            return null;
        Token token = getToken();
        //System.out.println("parsing atom: " + token.type() + " " + token.info());
        Node res;
        switch (token.type()) {
            case OPEN_BRACKET:
                movePtr();
                res = parseExpression();
                if(getToken().type() != TokenType.CLOSE_BRACKET)
                    throw new SyntaxException(getToken().line(), "close bracket expected");
                movePtr();
                break;
            case NEW:
                movePtr();
                Token signalClass = getToken();
                if (signalClass.type() != TokenType.FIELD)
                    throw new SyntaxException(getToken().line(), "class name expected");
                movePtr();
                res = createNode(CONSTRUCTOR, signalClass.info(), parseArguments());
                break;
            case NUMBER:
                res = createNode(NUMBER, token.info());
                movePtr();
                break;
            case TEXT:
                movePtr();
                return createNode(TEXT, token.info());
            case FIELD:
                res = createNode(OBJECT, token.info());
                movePtr();
                break;
            case OPERATOR:
                if (token.info().equals("-")) {
                    movePtr();
                    return createNode(UNARY_MINUS, null, parseAtom());
                }
            default:
                throw new SyntaxException(getToken().line(), "atom expected");
        }
        while (getToken().type() == TokenType.DOT) {
            movePtr();
            Token func = getToken();
            if(func.type() != TokenType.FIELD)
                throw new SyntaxException(getToken().line(), "expected function or socket name");
            movePtr();
            if(getToken().type() == TokenType.OPEN_BRACKET) {
                List<Node> args = new ArrayList<>();
                args.add(res);
                args.addAll(parseArguments());
                res = createNode(FUNCTION, func.info(), args);
            }
            else res = createNode(SOCKET, func.info(), res);
        }
        return res;
    }

    private boolean isArithmetic(Token token){
        return token.type() == TokenType.OPERATOR && List.of("+", "-", "*", "/").contains((String) token.info());
    }


    /**
     * if 0 then this is not a binary operator
     */
    private int getPrecedence(Token token) {
        if (token.type() != TokenType.OPERATOR)
            return 0;
        String operator = (String) token.info();
        if (List.of("=", ":=", "->", "=>", "->-", "<-", "<=", "-<-").contains(operator))
            return 1;
        if (List.of("+", "-").contains(operator))
            return 10;
        if (List.of("*", "/").contains(operator))
            return 20;
        throw new RuntimeException("unknown operator");
    }

    private Node maybeBinary(Node left, int precedence) throws SyntaxException {
        Token token = getToken();
        int curPrecedence = getPrecedence(token);
        if (curPrecedence > precedence) {
            movePtr();
            return maybeBinary(createNode(isArithmetic(token) ? ARITHMETIC_OPERATOR : ACTION,
                    token.info(),
                    left,
                    maybeBinary(parseAtom(), curPrecedence)), precedence);
        }
        return left;
    }

    private Node parseExpression() throws SyntaxException {
        return maybeBinary(parseAtom(), 0);
    }

    public List<Node> parse() throws SyntaxException {
        List<Node> res = new ArrayList<>();
        while (getToken().type() != TokenType.END_OF_CODE) {
            Token token = getToken();
            if(token.type() != TokenType.END_OF_LINE){
                if (token.type() == TokenType.MODE_CHANGE) {
                    res.add(createNode(MODE_CHANGE, token.info()));
                    movePtr();
                } else {
                    if (token.type() == TokenType.MODE_ONCE) {
                        movePtr();
                        res.add(createNode(MODE_ONCE, null, parseExpression()));
                    } else res.add(parseExpression());
                }
            }
            if (getToken().type() != TokenType.END_OF_LINE)
                throw new SyntaxException(getToken().line(), "expected end of line ");
            movePtr();
        }
        return res;
    }
}
