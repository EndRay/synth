import sources.Gated;
import sources.SignalProcessor;
import sources.SignalSource;
import sources.utils.Mixer;
import sources.utils.MultiGate;
import sources.utils.Socket;
import sources.utils.SourceValue;
import sources.voices.Voice;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static sources.SignalSource.frequencyRatioToVoltage;
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

class VoiceAndGlobalInteractionException extends Exception{}

class IsNotAProcessorException extends Exception {
    public IsNotAProcessorException(String message) {
        super(message);
    }
}

/**
 * output
 * vMix
 * aftertouchCh
 * v: pitch
 * v: velocity
 * v: aftertouch
 * v: releaseVelocity
 *
 * TODO: MIDI channels
 * TODO: Aliases
 * TODO: modulatable ADSR
 * TODO: exponential envelope stages
 * TODO: slope limiter
 * TODO: Effects
 * TODO: Stereo
 * TODO: Arithmetic-style operations
 *
 */

public class SynthBuilder {

    static final HashMap<String, Class<? extends SignalSource>> permittedClasses = new HashMap<>();
    static final Method[] signalMethods = SignalSource.class.getMethods();

    static {
        //String usableObjectsFilePath = "C:\\Users\\aikov\\Documents\\java_projects\\MySynth\\src\\UsableObjects";
        String usableObjectsFilePath = "src/UsableObjects";
        Pattern shiftSearcher = Pattern.compile("( {4}|\\t)");
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
    private final Socket[] voiceOutputs;
    private final Map<String, SignalSource> objects = new HashMap<>();
    private final Map<String, SignalSource[]> voiceObjects = new HashMap<>();

    private final MultiGate gated;
    private final MultiGate[] voiceGated;

    public enum EditMode {GLOBAL, VOICE}

    EditMode editMode;

    SynthBuilder(int voicesCount) {
        voices = new Voice[voicesCount];
        this.output = new Socket();
        voiceOutputs = new Socket[voicesCount];
        objects.put("aftertouchCh", new SourceValue("channel aftertouch"));
        voiceObjects.put("pitch", new SignalSource[voicesCount]);
        voiceObjects.put("velocity", new SignalSource[voicesCount]);
        voiceObjects.put("aftertouch", new SignalSource[voicesCount]);
        voiceObjects.put("releaseVelocity", new SignalSource[voicesCount]);
        gated = new MultiGate();
        voiceGated = new MultiGate[voicesCount];
        for (int i = 0; i < voicesCount; ++i) {
            voiceOutputs[i] = new Socket();
            SourceValue pitch = new SourceValue("voice #" + i + " frequency", frequencyToVoltage(440)),
                    velocity = new SourceValue("voice #" + i + " velocity", 0.5),
                    aftertouch = new SourceValue("voice #" + i + " aftertouch", 0),
                    releaseVelocity = new SourceValue("voice #" + i + " release velocity", 0);
            voiceObjects.get("pitch")[i] = pitch;
            voiceObjects.get("velocity")[i] = velocity;
            voiceObjects.get("aftertouch")[i] = aftertouch;
            voiceObjects.get("releaseVelocity")[i] = releaseVelocity;
            voiceGated[i] = new MultiGate();
            voices[i] = new Voice(pitch, velocity, aftertouch, releaseVelocity, voiceGated[i]);
        }
        synth = new MyPolySynth(voices, output, gated);
        objects.put("vMix", new Mixer(voiceOutputs));
    }

    public Synth getSynth() {
        return synth;
    }

    public void assignName(int voiceId, String name, SignalSource signal) {
        if(voiceId == -1) {
            objects.put(name, signal);
        }
        else{
            if (!voiceObjects.containsKey(name))
                voiceObjects.put(name, new SignalSource[voices.length]);
            voiceObjects.get(name)[voiceId] = signal;
        }
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

    private ObjClassPair[] getArguments(int voiceId, String args) throws IncorrectFormatException, NoSuchObjectException, NoSuchSignalException, NoSuchClassException, NoSuchConstructorException, NoSuchMethodException, VoiceAndGlobalInteractionException {
        String[] strArgs = getStrArguments(args);
        ObjClassPair[] res = new ObjClassPair[strArgs.length];
        for (int i = 0; i < strArgs.length; ++i)
            res[i] = parseArgument(voiceId, strArgs[i]);
        return res;
    }

    public SignalSource createNewSignal(int voiceId, Class<? extends SignalSource> objClass, ObjClassPair[] objClassArgs) throws NoSuchConstructorException, IncorrectFormatException {
        try {
            Object[] args = new Object[objClassArgs.length];
            Class<?>[] argTypes = new Class<?>[objClassArgs.length];
            for (int i = 0; i < objClassArgs.length; ++i) {
                args[i] = objClassArgs[i].getObj();
                argTypes[i] = objClassArgs[i].getObjClass();
            }
            SignalSource createdObj = objClass.getConstructor(argTypes).newInstance(args);
            if(createdObj instanceof Gated gatedObj){
                if(voiceId == -1)
                    gated.addDestination(gatedObj);
                else voiceGated[voiceId].addDestination(gatedObj);
            }
            if(createdObj instanceof SourceValue sourceValue){
                if(voiceId == -1)
                    synth.addToMap(sourceValue);
                else throw new IncorrectFormatException();
            }
            return createdObj;
        } catch (InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new NoSuchConstructorException();
        }
    }

    private SignalSource parseObject(int voiceId, String str, boolean onlyVoice) throws NoSuchObjectException, VoiceAndGlobalInteractionException {
        if(voiceId != -1) {
            if(voiceObjects.containsKey(str))
                return voiceObjects.get(str)[voiceId];
            else if(onlyVoice)
                throw new VoiceAndGlobalInteractionException();
        }
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
        if(arg.endsWith("x"))
            return frequencyRatioToVoltage(Double.parseDouble(arg.substring(0, arg.length() - 1)));
        return Double.parseDouble(arg);
    }

    private ObjClassPair parseArgument(int voiceId, String arg) throws NoSuchObjectException, IncorrectFormatException, NoSuchSignalException, NoSuchClassException, NoSuchConstructorException, NoSuchMethodException, VoiceAndGlobalInteractionException {
        arg = arg.trim();
        if(arg.startsWith("\"") && arg.endsWith("\""))
            return new ObjClassPair(arg.substring(1, arg.length()-1), String.class);
        try {
            return new ObjClassPair(parseDouble(arg), double.class);
        } catch (NumberFormatException e) {
            return new ObjClassPair(parseSignal(voiceId, arg), SignalSource.class);
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

    private SignalSource parseSignal(int voiceId, String str) throws NoSuchObjectException, IncorrectFormatException, NoSuchSignalException, NoSuchClassException, NoSuchConstructorException, NoSuchMethodException, VoiceAndGlobalInteractionException {
        str = str.trim();
        if (!str.endsWith(")"))
            return parseObject(voiceId, str, false);
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
                return createNewSignal(voiceId, permittedClasses.get(name), getArguments(voiceId, str.substring(bracePos + 1, str.length() - 1)));
            }
            if (str.startsWith("("))
                return parseSignal(voiceId, str.substring(1, str.length() - 1));
            System.out.println("function returning signal ??? (TODO (do i have one?))");
            throw new IncorrectFormatException();
        }
        SignalSource signal = parseSignal(voiceId, str.substring(0, start - 1));
        str = str.substring(start, str.length() - 1);
        int bracePos = str.indexOf("(");

        String name = str.substring(0, bracePos);
        ObjClassPair[] objClassArgs = getArguments(voiceId, str.substring(bracePos + 1));
        Object[] args = new Object[objClassArgs.length];
        Class<?>[] argTypes = new Class<?>[objClassArgs.length];
        for (int j = 0; j < objClassArgs.length; ++j) {
            args[j] = objClassArgs[j].getObj();
            argTypes[j] = objClassArgs[j].getObjClass();
            //System.out.println("arg " + j + ": " + args[j] + " " + argTypes[j]);
        }
        try {
            Method method = SignalSource.class.getMethod(name, argTypes);
            signal = (SignalSource) method.invoke(signal, args);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new NoSuchMethodException(str + ")");
        }
        return signal;
    }

    private Socket parseSocket(int voiceId, String str, boolean onlyVoice) throws NoSuchObjectException, IncorrectFormatException, NoSuchSocketException, VoiceAndGlobalInteractionException {
        str = str.trim();
        if(str.equals("output")) {
            return voiceId == -1 ? output : voiceOutputs[voiceId];
        }
        String[] split = str.split("\\.");
        if (split.length != 2) {
            if(split.length == 1 && parseObject(voiceId, str, onlyVoice) instanceof Socket socket)
                return socket;
            throw new IncorrectFormatException();
        }
        SignalSource obj = parseObject(voiceId, split[0], onlyVoice);
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

    private void handleCommand(int voiceId, String command) throws IncorrectFormatException, NoSuchConstructorException, NoSuchClassException, NoSuchObjectException, NoSuchSignalException, NoSuchMethodException, NoSuchSocketException, IsNotAProcessorException, VoiceAndGlobalInteractionException {
        command = command.trim();
        if (command.contains("->") || command.contains("<-") ||
                command.contains("=>") || command.contains("<=") ||
                command.contains("->-") || command.contains("-<-")) {
            String[] split = command.split("\\s*(->-|-<-|->|<-|=>|<=)\\s*");
            if (split.length != 2)
                throw new IncorrectFormatException();
            if (command.contains("->") || command.contains("=>") || command.contains("->-")) {
                SignalSource signal = parseSignal(voiceId, split[0]);
                if(command.contains("->-")){
                    Socket socket = parseSocket(voiceId, split[1], true);
                    if (!(signal instanceof SignalProcessor processor))
                        throw new IsNotAProcessorException(split[0]);
                    socket.process(processor);
                }
                else if (command.contains("=>")) {
                    Socket socket = parseSocket(voiceId, split[1], true);
                    socket.bind(signal);
                }
                if (command.contains("->")) {
                    Socket socket = parseSocket(voiceId, split[1], false);
                    socket.modulate(signal);
                }
            } else if (command.contains("<-") || command.contains("<=") || command.contains("-<-")) {
                String flippedArrow;
                if (command.contains("<-")) flippedArrow = "->";
                else if (command.contains("<=")) flippedArrow = "=>";
                else flippedArrow = "->-";
                handleCommand(voiceId, split[1] + flippedArrow + split[0]);
            }
            return;
        }

        if(command.contains(":=")){
            String[] tmp = command.split("\\s*:=\\s*");
            if(tmp.length != 2)
                throw new IncorrectFormatException();
            parseSocket(voiceId, tmp[0], false).set(parseDouble(tmp[1]));
            return;
        }
        if (command.contains("=")) {
            String[] tmp = command.split("\\s*=\\s*");
            if (tmp.length != 2)
                throw new IncorrectFormatException();
            assignName(voiceId, tmp[0], parseSignal(voiceId, tmp[1]));
            return;
        }
        throw new IncorrectFormatException();
    }

    public void handleCommand(String command){
        command = command.trim();
        if(command.startsWith("#"))
            return;
        if (command.isBlank())
            return;
        if(command.equals("map")){
            synth.startMapping();
            return;
        }
        if(command.equals("stop map")){
            synth.stopMapping();
            return;
        }
        try {
            if (command.startsWith("v: ")) {
                for (int i = 0; i < voices.length; ++i)
                    handleCommand(i, command.substring(3));
                return;
            }
            handleCommand(-1, command);
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
        } catch (VoiceAndGlobalInteractionException e) {
            System.out.println("voice things doing things with global things");
        } catch (NumberFormatException e){
            System.out.println("value expected");
        }
    }
}
