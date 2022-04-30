package sequencer;

import java.util.*;

public class Sequence implements Iterable<Step>{
    private final List<Step> steps = new ArrayList<>();

    public Sequence(Step... steps) {
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
}
