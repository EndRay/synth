package ui;

import synthesizer.sources.SignalProcessor;
import synthesizer.sources.SignalSource;
import synthesizer.sources.utils.*;
import synthesizer.sources.voices.Voice;
import synthesizer.Synth;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static synthesizer.sources.SignalSource.*;

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
 * aftertouchChannel
 *
 * lastNotePitch
 * lastNoteVelocity
 * lastNoteAftertouch
 * lastNoteReleaseVelocity
 * lastNoteGate
 * lastNoteTrigger
 *
 * v: output
 * v: pitch
 * v: velocity
 * v: aftertouch
 * v: releaseVelocity
 * v: gate
 * v: trigger
 *
 * TODO: Exponential envelope stages
 * TODO: Linear Slope limiter
 * TODO: Morph (simple and as Joranalogue's)
 * TODO: TZFM Oscillators
 * TODO: Variable Poles Filters
 * TODO: Highpass/Notch/Bandpass[/Morphable] Filters
 * TODO: (!!) Delayed SignalProcessors (???)
 * TODO: (!!!) MIDI files parsing
 * TODO: (!!!) To WAV
 * TODO: (!) lastNoteIsLegato Gate
 * TODO: (!!) Remove Dry/Wet on Effects
 * TODO: MIDI note map (for drums)
 *
 * TODO: NORMAL PARSING
 * TODO: Stereo
 */

/**
 * FIX!!!!!
 * load init.patch
 *
 * -v-
 * env = new ADSR(0.2s, 2s, 0.7, 0.2s)
 * env.gate <= gate
 * env.trig <= trigger
 *
 * lfo = new Sine(10s)
 * lfo.sync <= trigger
 *
 * osc = new Pulse(pitch)
 * osc.pw <- lfo * 0.2
 * osc * env => output
 *
 * filter = new Filter(0.3)
 * filter.f <- pitch
 * filter.f <- lfo * -0.3
 * env * 0.3 -> filter.f
 * filter ->- output
 * ---
 *
 * volume := 0
 */

public class SynthBuilder {

    static final HashMap<String, Class<? extends SignalSource>> permittedClasses = new HashMap<>();
    static final HashMap<String, String> socketAliases = new HashMap<>();
    //static final Method[] signalMethods = SignalSource.class.getMethods();

    static {
        String usableObjectsFilePath = "src/UsableObjects";
        String socketAliasesFilePath = "src/SocketAliases";
        try {
            Pattern shiftSearcher = Pattern.compile("( {4}|\\t)");
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
                    Class<? extends SignalSource> curClass = (Class<? extends SignalSource>) Class.forName("synthesizer." + res + "." + className);
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

        try{
            File socketAliasesFile = new File(socketAliasesFilePath);
            Scanner reader = new Scanner(socketAliasesFile);
            while(reader.hasNextLine()){
                String line = reader.nextLine();
                String[] tmp = line.split(" as ");
                String name = tmp[0].trim();
                if(tmp[0].contains(" ") || tmp[0].contains("\t"))
                    throw new IncorrectFormatException();
                String[] aliases = tmp[1].split(",");
                for(String alias : aliases)
                    socketAliases.put(alias.trim(), name);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IncorrectFormatException e) {
            System.out.println("Incorrect format in \"" + socketAliases + "\" file");
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

    private final List<String> commandsHistory = new ArrayList<>();

    public enum EditMode {GLOBAL, VOICE}

    EditMode editMode = EditMode.GLOBAL;

    SynthBuilder(int voicesCount) {
        voices = new Voice[voicesCount];
        this.output = new Socket();
        voiceOutputs = new Socket[voicesCount];
        SourceValue aftertouchChannel = new SourceValue("channel aftertouch");
        objects.put("aftertouchChannel", aftertouchChannel);

        SourceValue lastNotePitch = new SourceValue("last note pitch", frequencyToVoltage(440)),
                lastNoteVelocity = new SourceValue("last note velocity", 0.5),
                lastNoteAftertouch = new SourceValue("last note aftertouch"),
                lastNoteReleaseVelocity = new SourceValue("last note release velocity"),
                lastNoteGate = new SourceValue("last note gate");
        Triggerable lastNoteTrigger = new Triggerable("last note trigger");
        objects.put("lastNotePitch", lastNotePitch);
        objects.put("lastNoteVelocity", lastNoteVelocity);
        objects.put("lastNoteAftertouch", lastNoteAftertouch);
        objects.put("lastNoteReleaseVelocity", lastNoteVelocity);
        objects.put("lastNoteGate", lastNoteGate);
        objects.put("lastNoteTrigger", lastNoteTrigger);
        Voice last = new Voice(lastNotePitch, lastNoteVelocity, lastNoteAftertouch, lastNoteReleaseVelocity, lastNoteGate, lastNoteTrigger);

        voiceObjects.put("pitch", new SignalSource[voicesCount]);
        voiceObjects.put("velocity", new SignalSource[voicesCount]);
        voiceObjects.put("aftertouch", new SignalSource[voicesCount]);
        voiceObjects.put("releaseVelocity", new SignalSource[voicesCount]);
        voiceObjects.put("gate", new SignalSource[voicesCount]);
        voiceObjects.put("trigger", new SignalSource[voicesCount]);
        for (int i = 0; i < voicesCount; ++i) {
            voiceOutputs[i] = new Socket();
            SourceValue pitch = new SourceValue("voice #" + i + " pitch", frequencyToVoltage(440)),
                    velocity = new SourceValue("voice #" + i + " velocity", 0.5),
                    aftertouch = new SourceValue("voice #" + i + " aftertouch"),
                    releaseVelocity = new SourceValue("voice #" + i + " release velocity"),
                    gate = new SourceValue("voice #" + i + " gate");
            Triggerable trigger = new Triggerable("voice #" + i + " trigger");
            voiceObjects.get("pitch")[i] = pitch;
            voiceObjects.get("velocity")[i] = velocity;
            voiceObjects.get("aftertouch")[i] = aftertouch;
            voiceObjects.get("releaseVelocity")[i] = releaseVelocity;
            voiceObjects.get("gate")[i] = gate;
            voiceObjects.get("trigger")[i] = trigger;
            voices[i] = new Voice(pitch, velocity, aftertouch, releaseVelocity, gate, trigger);
        }
        synth = new Synth(voices, output, last);
        objects.put("voiceMix", new Mixer(voiceOutputs));
    }

    public Synth getSynth() {
        return synth;
    }

    public List<String> getHistory(){
        return commandsHistory;
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

    private int getLast(String str, char ch) throws IncorrectFormatException {
        int braces = 0;
        for (int i = str.length() - 1; i >= 0; --i) {
            if (str.charAt(i) == '(') {
                --braces;
                if (braces < 0)
                    throw new IncorrectFormatException();
            } else if (str.charAt(i) == ')') {
                ++braces;
            }
            if (braces == 0 && str.charAt(i) == ch)
                return i;
        }
        if (braces != 0)
            throw new IncorrectFormatException();
        return -1;
    }

    private int getFirst(String str, String ch) throws IncorrectFormatException {
        return getFirst(str, ch, 0);
    }

    private int getFirst(String str, String ch, int firstIndex) throws IncorrectFormatException {
        int braces = 0;
        for (int i = 0; i < str.length(); ++i) {
            if (str.charAt(i) == '(') {
                ++braces;

            } else if (str.charAt(i) == ')') {
                --braces;
                if (braces < 0)
                    throw new IncorrectFormatException();
            }
            if (i >= firstIndex && braces == 0 && ch.contains(""+str.charAt(i)))
                return i;
        }
        if (braces != 0)
            throw new IncorrectFormatException();
        return str.length();
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
        try {
            int firstInfix = getFirst(arg, "+-", 1);
            if (firstInfix != arg.length()) {
                if(arg.charAt(firstInfix) == '+')
                    return parseDouble(arg.substring(0, firstInfix)) + parseDouble(arg.substring(firstInfix + 1));
                else return parseDouble(arg.substring(0, firstInfix)) - parseDouble(arg.substring(firstInfix + 1));
            }
            firstInfix = getFirst(arg, "*/");
            if (firstInfix != arg.length()) {
                if(arg.charAt(firstInfix) == '*')
                    return parseDouble(arg.substring(0, firstInfix)) * parseDouble(arg.substring(firstInfix + 1));
                return parseDouble(arg.substring(0, firstInfix)) / parseDouble(arg.substring(firstInfix+1));
            }
            if(arg.startsWith("(") && arg.endsWith(")"))
                return parseDouble(arg.substring(1, arg.length()-1));
        } catch (IncorrectFormatException e){
            throw new NumberFormatException();
        }
        if(arg.endsWith("hz"))
            return frequencyToVoltage(Double.parseDouble(arg.substring(0, arg.length() - 2)));
        if(arg.endsWith("ms"))
            return timeToVoltage(Double.parseDouble(arg.substring(0, arg.length() - 2)) / 1000);
        if(arg.endsWith("s"))
            return timeToVoltage(Double.parseDouble(arg.substring(0, arg.length()-1)));
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
        int firstInfix = getFirst(str, "+-", 1);
        if(firstInfix != str.length()) {
            if(str.charAt(firstInfix) == '+')
                return parseSignal(voiceId, str.substring(0, firstInfix)).add(parseSignal(voiceId, str.substring(firstInfix + 1)));
            else return parseSignal(voiceId, str.substring(0, firstInfix)).sub(parseSignal(voiceId, str.substring(firstInfix + 1)));
        }
        firstInfix = getFirst(str, "*");
        if(firstInfix != str.length())
            return parseSignal(voiceId, str.substring(0, firstInfix)).attenuate(parseSignal(voiceId, str.substring(firstInfix + 1)));
        if(str.startsWith("-"))
            return parseSignal(voiceId, str.substring(1)).attenuate(-1);
        if (!str.endsWith(")")) {
            try{
                return new DC(parseDouble(str));
            } catch (NumberFormatException ignore){}
            return parseObject(voiceId, str, false);
        }
        int start = getLast(str, '.')+1;
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
        return (Socket) parseSocket(voiceId, str, onlyVoice, true);
    }

    private PseudoSocket parseSocket(int voiceId, String str, boolean onlyVoice, boolean trueSocket) throws NoSuchObjectException, IncorrectFormatException, NoSuchSocketException, VoiceAndGlobalInteractionException {
        str = str.trim();
        if(str.equals("output")) {
            return voiceId == -1 ? output : voiceOutputs[voiceId];
        }
        String[] split = str.split("\\.");
        if (split.length != 2) {
            if(split.length == 1){
                SignalSource signal = parseObject(voiceId, str, onlyVoice);
                if(signal instanceof Socket socket)
                    return socket;
                if(!trueSocket && signal instanceof PseudoSocket pseudo)
                    return pseudo;
            }
            throw new IncorrectFormatException();
        }
        if(socketAliases.containsKey(split[1]))
            split[1] = socketAliases.get(split[1]);
        SignalSource obj = parseObject(voiceId, split[0], onlyVoice);
        Method[] methods = obj.getClass().getMethods();
        Optional<Method> method = Arrays.stream(methods).filter(x -> x.getName().equals(split[1])).findAny();
        if (method.isEmpty())
            throw new NoSuchSocketException(split[1]);
        try {
            return (PseudoSocket) method.get().invoke(obj);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new NoSuchSocketException(split[1]);
        }
    }

    private void parseCommand(int voiceId, String command) throws IncorrectFormatException, NoSuchConstructorException, NoSuchClassException, NoSuchObjectException, NoSuchSignalException, NoSuchMethodException, NoSuchSocketException, IsNotAProcessorException, VoiceAndGlobalInteractionException {
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
                    PseudoSocket socket = parseSocket(voiceId, split[1], true, false);
                    socket.bind(signal);
                }
                if (command.contains("->")) {
                    PseudoSocket socket = parseSocket(voiceId, split[1], false, false);
                    socket.modulate(signal);
                }
            } else if (command.contains("<-") || command.contains("<=") || command.contains("-<-")) {
                String flippedArrow;
                if (command.contains("<-")) flippedArrow = "->";
                else if (command.contains("<=")) flippedArrow = "=>";
                else flippedArrow = "->-";
                parseCommand(voiceId, split[1] + flippedArrow + split[0]);
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

    private void parseCommand(String command) throws NoSuchConstructorException, IsNotAProcessorException, NoSuchSocketException, NoSuchClassException, IncorrectFormatException, NoSuchObjectException, VoiceAndGlobalInteractionException, NoSuchSignalException, NoSuchMethodException {
        if (command.isBlank())
            return;
        command = command.trim();
        if(command.startsWith("#"))
            return;

        if(command.equals("map")){
            synth.startMapping();
            return;
        }
        if(command.equals("stop map")){
            synth.stopMapping();
            return;
        }
        if(command.equals("-v-")) {
            editMode = EditMode.VOICE;
            return;
        }
        if(command.equals("---")){
            editMode = EditMode.GLOBAL;
            return;
        }
        EditMode mode = editMode;
        if(command.startsWith("v: ")) {
            command = command.substring(3);
            mode = EditMode.VOICE;
        }
        else if(command.startsWith("g: ")) {
            command = command.substring(3);
            mode = EditMode.GLOBAL;
        }
        if (mode == EditMode.VOICE) {
            for (int i = 0; i < voices.length; ++i)
                parseCommand(i, command);
        }
        else parseCommand(-1, command);
    }

    public void handleCommand(String command) throws NoSuchConstructorException, IsNotAProcessorException, NoSuchSocketException, NoSuchClassException, FileNotFoundException, IncorrectFormatException, NoSuchObjectException, VoiceAndGlobalInteractionException, NoSuchSignalException, NoSuchMethodException {
        if (command.isBlank())
            return;
        command = command.trim();
        if(command.matches("load .+")){
            File file = new File("patches/" + command.substring(4).trim());
            Scanner reader = new Scanner(file);
            while (reader.hasNextLine())
                handleCommand(reader.nextLine());
            return;
        }
        parseCommand(command);
        commandsHistory.add(command);
    }
}
