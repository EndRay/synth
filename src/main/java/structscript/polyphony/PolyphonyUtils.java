package structscript.polyphony;

import java.util.Set;

public class PolyphonyUtils {
    private PolyphonyUtils(){}
    public static PolyphonyType byString(String polyphony) throws PolyphonyException {
        if(Set.of("mono", "monophonic").contains(polyphony))
            return new Monophonic();
        try{
            int voices = Integer.parseInt(polyphony);
            if(voices < 0)
                throw new PolyphonyException();
            if(voices == 0)
                return new Nophonic();
            return new Polyphonic(voices);
        } catch (NumberFormatException ignore){}
        throw new PolyphonyException();
    }
}
