package ui.cui;

import synthesizer.sources.utils.SourceValue;
import ui.structscript.SourceValuesHandler;

import java.util.*;

public class CCSourceValuesHandler implements SourceValuesHandler {

    LinkedList<SourceValue> valuesToMap = new LinkedList<>();
    Map<Integer, SourceValue> valueByCC = new HashMap<>();
    boolean nowMapping = false;

    Set<Integer> forbiddenCCs = new HashSet<>();
    {
        for(int i = 32; i <= 63; ++i)
            forbiddenCCs.add(i);
        for(int i = 120; i <= 127; ++i)
            forbiddenCCs.add(i);
    }

    public CCSourceValuesHandler(){}

    private void tryToMap(){
        if(!nowMapping || valuesToMap.isEmpty())
            return;
        System.out.println("Mapping \"" + valuesToMap.getFirst().getName() + "\"");
    }

    public void startMapping(){
        nowMapping = true;
        tryToMap();
    }

    public void stopMapping(){
        nowMapping = false;
    }

    public void midiCC(int CC, int value) {
        if (!valueByCC.containsKey(CC)) {
            if (!nowMapping || valuesToMap.isEmpty() || forbiddenCCs.contains(CC))
                return;
            valueByCC.put(CC, valuesToMap.removeFirst());
            System.out.println("\"" + valueByCC.get(CC).getName() + "\" mapped to CC" + CC);
            tryToMap();
        }
        valueByCC.get(CC).setValue(value / 128.0);
    }

    @Override
    public void addSourceValue(SourceValue sourceValue) {
        valuesToMap.add(sourceValue);
    }
}
