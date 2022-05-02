package sequencer;

import java.util.*;

public class Sequence implements Iterable<Step>{
    private MeasureDivision measureDivision;
    private final List<Step> steps = new ArrayList<>();

    public Sequence(Step... steps) {
        this(MeasureDivision.SIXTEENTH, steps);
    }

    public Sequence(MeasureDivision measureDivision, Step... steps) {
        this.measureDivision = measureDivision;
        Collections.addAll(this.steps, steps);
    }

    public void addStep(Step step){
        steps.add(step);
    }

    public Step removeStep(){
        return steps.remove(steps.size() - 1);
    }

    public List<Step> getSteps(){
        return Collections.unmodifiableList(steps);
    }

    @Override
    public ListIterator<Step> iterator() {
        return getSteps().listIterator();
    }

    public MeasureDivision getMeasureDivision() {
        return measureDivision;
    }

    public void setMeasureDivision(MeasureDivision measureDivision) {
        this.measureDivision = measureDivision;
    }
}
