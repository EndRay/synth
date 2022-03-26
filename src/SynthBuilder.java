import sources.Gated;
import sources.SignalProcessor;
import sources.SignalSource;
import sources.Triggerable;
import sources.utils.*;
import sources.voices.Voice;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static sources.SignalSource.frequencyToVoltage;

class IncorrectFormatException extends Exception {
}

class IncorrectShiftsException extends Exception {
}

class NoSuchSocketException extends Exception {
    public NoSuchSocketException(String message) {
        super(message);
    }
}

class NoSuchSignalException extends Exception {
    public NoSuchSignalException(String message) {
        super(message);
    }
}

class NoSuchObjectException extends Exception {
    public NoSuchObjectException(String message) {
        super(message);
    }
}

class NoSuchClassException extends Exception {
    public NoSuchClassException(String message) {
        super(message);
    }
}

class NoSuchConstructorException extends Exception {
}

class IsNotAProcessorException extends Exception {
    public IsNotAProcessorException(String message) {
        super(message);
    }
}

public class SynthBuilder {

    static final HashMap<String, Class<? extends SignalSource>> permittedClasses = new HashMap<>();
    static final Method[] signalMethods = SignalSource.class.getMethods();

    static {
        String usableObjectsFilePath = "C:\\Users\\aikov\\Documents\\java_projects\\MySynth\\src\\UsableObjects";
        Pattern shiftSearcher = Pattern.compile("(    |\\t)");
        try {
            File usable = new File(usableObjectsFilePath);
            Scanner reader = new Scanner(usable);
            int lastShift = -1;
            Stack<String> classPath = new Stack<>();
            while (true) {
// !!! remake shifts parsing
                boolean isLastLine = false;
                String data = "";
                if (reader.hasNextLine()) {
                    data = reader.nextLine();
                    if (data.isBlank())
                        continue;
                } else isLastLine = true;
                Matcher m = shiftSearcher.matcher(data);
                int shift = (int) m.results().count();
                data = m.replaceAll("");
                if (!isLastLine && data.startsWith(" "))
                    throw new IncorrectFormatException();
                if (lastShift < shift - 1)
                    throw new IncorrectShiftsException();
                if (lastShift >= shift) {
                    // then previous data was class

                    String lastLine = classPath.pop();
                    String res = String.join(".", classPath);
                    String[] split = lastLine.split("\\s+as\\s+");
                    if (split.length == 0 || split.length > 2)
                        throw new IncorrectFormatException();
                    String className = split[0];
// ??? what to do
                    @SuppressWarnings("unchecked")
                    Class<? extends SignalSource> curClass = (Class<? extends SignalSource>) Class.forName(res + "." + className);
                    permittedClasses.put(className, curClass);
                    if (split.length == 2) {
                        String[] aliases = split[1].split("\\s*,\\s*");
                        for (String alias : aliases)
                            permittedClasses.put(alias, curClass);
                    }
                    while (--lastShift >= shift)
                        classPath.pop();
                }
                lastShift = shift;
                classPath.push(data);
                if (isLastLine)
                    break;
            }
        } catch (FileNotFoundException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IncorrectShiftsException e) {
            System.out.println("Incorrect shifts in \"" + usableObjectsFilePath + "\" file");
            e.printStackTrace();
            System.exit(1);
        } catch (IncorrectFormatException e) {
            System.out.println("Incorrect format in \"" + usableObjectsFilePath + "\" file");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private final Synth synth;
    private final Voice[] voices;
    private final Socket output;
    Map<String, SignalSource> objects;
    Map<String, SignalSource[]> voiceObjects;

    public enum EditMode {GLOBAL, VOICE}

    EditMode editMode;

    SynthBuilder(int voices_count) {
        voices = new Voice[voices_count];
        this.output = new Socket(new Mixer(voices_count));
        for (int i = 0; i < voices_count; ++i) {
            SourceValue pitch = new SourceValue("voice #" + i + " frequency", 440),
                    velocity = new SourceValue("voice #" + i + " velocity", 0.5),
                    aftertouch = new SourceValue("voice #" + i + " aftertouch", 0),
                    releaseVelocity = new SourceValue("voice #" + i + " release velocity", 0);
            Gated gated = new MultiGate();
            Triggerable triggerable = new MultiTrigger();
            voices[i] = new Voice(pitch, velocity, aftertouch, releaseVelocity, gated, triggerable);
        }
        synth = new MyPolySynth(voices, output);
        objects = new HashMap<>();
        voiceObjects = new HashMap<>();
    }

    public Synth getSynth() {
        return synth;
    }

    public void assignName(String name, SignalSource signal) {
        if (objects.containsKey(name))
            System.out.println(name + " reassigning");
        objects.put(name, signal);
    }

    private int getEndOfFunc(String str, int i) throws IncorrectFormatException {
        int braces = 0;
        for (; i < str.length(); ++i) {
            if (str.charAt(i) == '(') {
                ++braces;
            } else if (str.charAt(i) == ')') {
                --braces;
                if (braces < 0)
                    throw new IncorrectFormatException();
                if (braces == 0)
                    return i + 1;
            }
        }
        throw new IncorrectFormatException();
    }

    private int getStartOfLastFunc(String str) throws IncorrectFormatException {
        int braces = 0;
        for (int i = str.length() - 1; i >= 0; --i) {
            if (str.charAt(i) == '(') {
                --braces;
                if (braces < 0)
                    throw new IncorrectFormatException();
            } else if (str.charAt(i) == ')') {
                ++braces;
            }
            if (braces == 0 && str.charAt(i) == '.')
                return i + 1;
        }
        if (braces != 0)
            throw new IncorrectFormatException();
        return 0;
    }

    private int getEndOfArgument(String str, int i) throws IncorrectFormatException {
        int braces = 0;
        for (; i < str.length(); ++i) {
            if (str.charAt(i) == '(') {
                ++braces;
            } else if (str.charAt(i) == ')') {
                --braces;
                if (braces < 0)
                    throw new IncorrectFormatException();
            }
            if (braces == 0 && str.charAt(i) == ',')
                return i;
        }
        return str.length();
    }

    private String[] getStrArguments(String args) throws IncorrectFormatException {
        List<String> strArgs = new ArrayList<>();
        for (int j = 0; j < args.length(); ) {
            int argEnd = getEndOfArgument(args, j);
            String strArg = args.substring(j, argEnd).trim();
            if (strArg.isBlank())
                break;
            strArgs.add(strArg);
            j = argEnd + 1;
        }
        return strArgs.toArray(new String[0]);
    }

    private ObjClassPair[] getArguments(String args) throws IncorrectFormatException, NoSuchObjectException, NoSuchSignalException, NoSuchClassException, NoSuchConstructorException, NoSuchMethodException {
        String[] strArgs = getStrArguments(args);
        ObjClassPair[] res = new ObjClassPair[strArgs.length];
        for (int i = 0; i < strArgs.length; ++i)
            res[i] = parseArgument(strArgs[i]);
        return res;
    }

    public SignalSource createNewSignal(Class<? extends SignalSource> objClass, ObjClassPair[] objClassArgs) throws NoSuchConstructorException {
        try {
            Object[] args = new Object[objClassArgs.length];
            Class<?>[] argTypes = new Class<?>[objClassArgs.length];
            for (int i = 0; i < objClassArgs.length; ++i) {
                args[i] = objClassArgs[i].getObj();
                argTypes[i] = objClassArgs[i].getObjClass();
            }
            return objClass.getConstructor(argTypes).newInstance(args);
        } catch (InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new NoSuchConstructorException();
        }
    }

    private SignalSource parseObject(String str) throws NoSuchObjectException {
        if (!objects.containsKey(str))
            throw new NoSuchObjectException(str);
        return objects.get(str);
    }

    record ObjClassPair(Object obj, Class<?> objClass) {

        public Object getObj() {
            return obj;
        }

        public Class<?> getObjClass() {
            return objClass;
        }
    }

    private double parseDouble(String arg) throws NumberFormatException{
        arg = arg.trim();
        if(arg.endsWith("hz"))
            return frequencyToVoltage(Double.parseDouble(arg.substring(0, arg.length() - 2)));
        return Double.parseDouble(arg);
    }

    private ObjClassPair parseArgument(String arg) throws NoSuchObjectException, IncorrectFormatException, NoSuchSignalException, NoSuchClassException, NoSuchConstructorException, NoSuchMethodException {
        try {
            return new ObjClassPair(parseDouble(arg), double.class);
        } catch (NumberFormatException e) {
            return new ObjClassPair(parseSignal(arg), SignalSource.class);
        }
    }

/*
f = new TwoPole()
f2 = new TwoPole()
f.sub(f2.mapBi(3, 7.2))
 */

/*
tri = (new Tri(7).mapBi(7, 3.2)).attenuate(23).mapUni(21)
 */

    private SignalSource parseSignal(String str) throws NoSuchObjectException, IncorrectFormatException, NoSuchSignalException, NoSuchClassException, NoSuchConstructorException, NoSuchMethodException {
        System.out.println("parsing: " + str);
        str = str.trim();
        if (!str.endsWith(")"))
            return parseObject(str);
        int start = getStartOfLastFunc(str);
        if (start == 0) {
            if (str.startsWith("new ")) {
                str = str.substring(3).trim();
                int bracePos = str.indexOf("(");
                if (bracePos == -1 || !str.endsWith(")"))
                    throw new IncorrectFormatException();
                String name = str.substring(0, bracePos);
                if (!permittedClasses.containsKey(name))
                    throw new NoSuchClassException(name);
                return createNewSignal(permittedClasses.get(name), getArguments(str.substring(bracePos + 1, str.length() - 1)));
            }
            if (str.startsWith("("))
                return parseSignal(str.substring(1, str.length() - 1));
            System.out.println("this is function returning signal ??? (TODO (do i have one?))");
            throw new RuntimeException();
        }
        SignalSource signal = parseSignal(str.substring(0, start - 1));
        str = str.substring(start, str.length() - 1);
        int bracePos = str.indexOf("(");

        String name = str.substring(0, bracePos);
        ObjClassPair[] objClassArgs = getArguments(str.substring(bracePos + 1));
        Object[] args = new Object[objClassArgs.length];
        Class<?>[] argTypes = new Class<?>[objClassArgs.length];
        for (int j = 0; j < objClassArgs.length; ++j) {
            args[j] = objClassArgs[j].getObj();
            argTypes[j] = objClassArgs[j].getObjClass();
            System.out.println("arg " + j + ": " + args[j] + " " + argTypes[j]);
        }
        try {
            System.out.println("getting method " + name + " with args " + Arrays.toString(args));
            Method method = SignalSource.class.getMethod(name, argTypes);
            signal = (SignalSource) method.invoke(signal, args);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new NoSuchMethodException(str + ")");
        }
        return signal;
    }

    private Socket parseSocket(String str) throws NoSuchObjectException, IncorrectFormatException, NoSuchSocketException {
        System.out.println("parsing socket: " + str);
        str = str.trim();
        if(str.equals("output"))
            return output;
        String[] split = str.split("\\.");
        if (split.length != 2) {
            if(split.length == 1 && objects.get(str) instanceof Socket socket)
                return socket;
            throw new IncorrectFormatException();
        }
        if (editMode == EditMode.VOICE) {
            System.out.println("TODO");
            throw new UnsupportedOperationException();
        }
        SignalSource obj = parseObject(split[0]);
        Method[] methods = obj.getClass().getMethods();
        Optional<Method> method = Arrays.stream(methods).filter(x -> x.getName().equals(split[1])).findAny();
        if (method.isEmpty())
            throw new NoSuchSocketException(split[1]);
        try {
            return (Socket) method.get().invoke(obj);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new NoSuchSocketException(split[1]);
        }
    }

    public void handleCommand(String command) {
        try {
            command = command.trim();
            if (command.isBlank())
                return;
            if (command.contains("->") || command.contains("<-") ||
                    command.contains("=>") || command.contains("<=") ||
                    command.contains("->-") || command.contains("-<-")) {
                String[] split = command.split("\\s*(->-|-<-|->|<-|=>|<=)\\s*");
                if (split.length != 2)
                    throw new IncorrectFormatException();
                if (command.contains("->") || command.contains("=>") || command.contains("->-")) {
                    SignalSource signal = parseSignal(split[0]);
                    System.out.println("PARSED");
                    Socket socket = parseSocket(split[1]);
                    if(command.contains("->-")){
                        if (!(signal instanceof SignalProcessor processor))
                            throw new IsNotAProcessorException(split[0]);
                        socket.process(processor);
                    }
                    if (command.contains("->"))
                        socket.modulate(signal);
                    else if (command.contains("=>"))
                        socket.bind(signal);
                } else if (command.contains("<-") || command.contains("<=") || command.contains("-<-")) {
                    String flippedArrow;
                    if (command.contains("<-")) flippedArrow = "->";
                    else if (command.contains("<=")) flippedArrow = "=>";
                    else flippedArrow = "->-";
                    handleCommand(split[1] + flippedArrow + split[0]);
                }
                return;
            }

            if(command.contains(":=")){
                String[] tmp = command.split("\\s*:=\\s*");
                if(tmp.length != 2)
                    throw new IncorrectFormatException();
                parseSocket(tmp[0]).set(parseDouble(tmp[1]));
                return;
            }
            if (command.contains("=")) {
                String[] tmp = command.split("\\s*=\\s*");
                if (tmp.length != 2)
                    throw new IncorrectFormatException();
                assignName(tmp[0], parseSignal(tmp[1]));
                return;
            }
            System.out.println(parseSignal(command));
//            System.out.println(command + " is " + parseObject(command).getClass().getSimpleName());
        } catch (IncorrectFormatException e) {
            System.out.println("incorrect format");
        } catch (NoSuchClassException e) {
            System.out.println("no such class \"" + e.getMessage() + "\"");
        } catch (NoSuchObjectException e) {
            System.out.println("no such object \"" + e.getMessage() + "\"");
        } catch (NoSuchSocketException e) {
            System.out.println("no such socket \"" + e.getMessage() + "\"");
        } catch (NoSuchSignalException e) {
            System.out.println("no such signal \"" + e.getMessage() + "\"");
        } catch (NoSuchMethodException e){
            System.out.println("no such method \"" + e.getMessage() + "\"");
        } catch (NoSuchConstructorException e) {
            System.out.println("no such constructor");
        } catch (IsNotAProcessorException e) {
            System.out.println(e.getMessage() + " is not a processor");
        }
    }
}
