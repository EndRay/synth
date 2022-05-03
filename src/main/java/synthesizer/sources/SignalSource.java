package synthesizer.sources;

import synthesizer.sources.utils.*;

public interface SignalSource {
    int sampleRate = 44100;
    double tuningFrequency = 440;
    double samplingPeriod = 1.0 / sampleRate;
    double minFrequency = 1, maxFrequency = 20000;

    double getSample(int sampleId);

    default double getFrequency(int sampleId){
        return SignalSource.voltageToFrequency(getSample(sampleId));
    }
    default double getTime(int sampleId){
        return SignalSource.voltageToTime(getSample(sampleId));
    }

    default boolean getGate(int sampleId){
        return getSample(sampleId) > 0.5;
    }

    static double frequencyToVoltage(double frequency) {
        return Math.log(frequency / minFrequency) / Math.log(maxFrequency / minFrequency);
    }

    static double voltageToFrequency(double voltage) {
        return Math.pow(maxFrequency / minFrequency, voltage) * minFrequency;
    }

    static double timeToVoltage(double time){
        return frequencyToVoltage(1/time);
    }

    static double voltageToTime(double voltage){
        return 1/voltageToFrequency(voltage);
    }

    static double frequencyRatioToVoltage(double frequencyRatio){
        return Math.log(frequencyRatio) / Math.log(maxFrequency / minFrequency);
    }

    default SignalSource attenuate(double coefficient){
        return new Attenuator(this, new DC(coefficient));
    }

    default SignalSource attenuate(SignalSource coefficientSource){
        return new Attenuator(this, coefficientSource);
    }

    default SignalSource add(double value){
        return this.add(new DC(value));
    }

    default SignalSource add(SignalSource valueSource){
        return new Mixer(this, valueSource);
    }

    default SignalSource sub(double value){
        return this.add(-value);
    }

    default SignalSource sub(SignalSource valueSource){
        return this.add(valueSource.attenuate(-1));
    }

    default SignalSource mapUni(double min, double max){
        return this.mapUni(new DC(min), new DC(max));
    }
    default SignalSource mapBi(double min, double max){
        return this.mapBi(new DC(min), new DC(max));
    }

    default SignalSource mapUni(SignalSource min, SignalSource max){
        return this.attenuate(max.sub(min)).add(min);
    }
    default SignalSource mapBi(SignalSource min, SignalSource max){
        return this.attenuate(max.sub(min).attenuate(0.5)).add(min.add(max).attenuate(0.5));
    }

    default SignalSource map(double minFrom, double maxFrom, double min, double max){
        return this.sub(minFrom).attenuate((max-min)/(maxFrom-minFrom)).add(min);
    }

    default SignalSource clipUni(){
        return new Clip(this, false);
    }

    default SignalSource clipBi(){
        return new Clip(this, true);
    }

    default SignalSource multiplyFrequency(double ratio){
        return this.add(frequencyRatioToVoltage(ratio));
    }

    default SignalSource addFrequency(double frequency){
        return new FrequencyAdder(this, frequency);
    }

    default SignalSource toFrequency(){
        return new FrequencyConverter(this);
    }

    default SignalSource inverse() {return new Inverser(this);}
}
