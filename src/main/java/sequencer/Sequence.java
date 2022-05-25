package sequencer;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Sequence{
    private final ReadWriteLock l = new ReentrantReadWriteLock();
    private MeasureDivision measureDivision;
    private Double defaultGate = 0.5;
    private Integer defaultVelocity = 60;
    private final List<Step> steps = new ArrayList<>();

    public Sequence(Step... steps) {
        this(MeasureDivision.SIXTEENTH, steps);
    }

    public void setDefaultGate(Double gate){
        try{
            l.writeLock().lock();
            defaultGate = gate;
        } finally {
            l.writeLock().unlock();
        }
    }

    public void setDefaultVelocity(Integer velocity){
        try{
            l.writeLock().lock();
            defaultVelocity = velocity;
        } finally {
            l.writeLock().unlock();
        }
    }

    public Sequence(MeasureDivision measureDivision, Step... steps) {
        this.measureDivision = measureDivision;
        Collections.addAll(this.steps, steps);
    }

    public void addStep(Step step){
        try {
            l.writeLock().lock();
            steps.add(step);
        } finally {
            l.writeLock().unlock();
        }
    }

    public Step removeStep(){
        try {
            l.writeLock().lock();
            return steps.remove(steps.size() - 1);
        } finally {
            l.writeLock().unlock();
        }
    }

    public int length(){
        try {
            l.readLock().lock();
            return steps.size();
        } finally {
            l.readLock().unlock();
        }
    }

    public Step getStep(int ind){
        try {
            l.readLock().lock();
            if(ind < 0 || ind >= length())
                throw new IndexOutOfBoundsException();
            return steps.get(ind);
        } finally {
            l.readLock().unlock();
        }
    }
    public List<Step> getSteps(){
        try {
            l.readLock().lock();
            return Collections.unmodifiableList(steps);
        } finally {
            l.readLock().unlock();
        }
    }

    public MeasureDivision getMeasureDivision() {
        try {
            l.readLock().lock();
            return measureDivision;
        } finally {
            l.readLock().unlock();
        }
    }

    public void setMeasureDivision(MeasureDivision measureDivision) {
        try {
            l.writeLock().lock();
            this.measureDivision = measureDivision;
        } finally {
            l.writeLock().unlock();
        }
    }

    public Double getDefaultGate() {
        return defaultGate;
    }

    public Integer getDefaultVelocity() {
        return defaultVelocity;
    }
}
