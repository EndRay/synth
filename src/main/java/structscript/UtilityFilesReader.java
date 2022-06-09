package structscript;

import synthesizer.sources.SignalSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BAD CODE ALERT!!!
 */

public class UtilityFilesReader {

    public static Map<String, String> getSocketAliases() throws UtilityFileException {
        Map<String, String> socketAliases = new HashMap<>();
        Scanner reader = new Scanner(UtilityFilesReader.class.getResourceAsStream("SocketAliases"));
        while(reader.hasNextLine()){
            String line = reader.nextLine();
            String[] tmp = line.split(" as ");
            String name = tmp[0].trim();
            if(tmp[0].contains(" ") || tmp[0].contains("\t"))
                throw new UtilityFileException("multiple words before \"as\"");
            String[] aliases = tmp[1].split(",");
            for(String alias : aliases)
                socketAliases.put(alias.trim(), name);
        }
        return socketAliases;
    }

    public static Map<String, Class<? extends SignalSource>> getPermittedClasses() throws UtilityFileException {
        try {
            Map<String, Class<? extends SignalSource>> permittedClasses = new HashMap<>();
            Pattern shiftSearcher = Pattern.compile("( {4}|\\t)");
            Scanner reader = new Scanner(UtilityFilesReader.class.getResourceAsStream("PermittedClasses"));
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
                    throw new UtilityFileException("incorrect shifts");
                if (lastShift < shift - 1)
                    throw new UtilityFileException("incorrect shifts");
                if (lastShift >= shift) {
                    // then previous data was class

                    String lastLine = classPath.pop();
                    String res = String.join(".", classPath);
                    String[] split = lastLine.split("\\s+as\\s+");
                    if (split.length > 2)
                        throw new UtilityFileException("more than one \"as\" per line are not allowed");
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
            return permittedClasses;
        } catch (ClassNotFoundException e) {
            throw new UtilityFileException("class not found");
        }
    }
}
