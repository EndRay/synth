//package realisations.effects;
//
//import sources.realisations.effects.AbstractEffect;
//import sources.utils.Socket;
//
//import static java.lang.Math.*;
//
//public class Compressor extends AbstractEffect {
//    final private Socket attack = new Socket(),
//            release = new Socket(),
//            threshold = new Socket(),
//            ratio = new Socket(),
//            gain = new Socket(),
//            makeup = new Socket();
//    private double currentReduction = 0;
//    private double lastSample = 0;
//
//    Compressor(double attack, double release, double threshold, double ratio, double gain, double makeup) {
//        this.attack.set(attack);
//        this.release.set(release);
//        this.threshold.set(threshold);
//        this.ratio.set(ratio);
//        this.gain.set(gain);
//        this.makeup.set(makeup);
//    }
//
//    public Socket attack(){ return attack; }
//    public Socket release(){ return release; }
//    public Socket threshold(){ return threshold; }
//    public Socket ratio(){ return ratio; }
//    public Socket gain(){ return gain; }
//    public Socket makeup(){ return makeup; }
//
//
//    @Override
//    public double getWetSample(int sampleId) {
//        if(checkAndUpdateSampleId(sampleId)){
//            lastSample = source().getSample(sampleId);
//            double lastSampleAmp = abs(lastSample);
//            if(lastSampleAmp > threshold().getSample(sampleId))
//                currentReduction = min(currentReduction + 1/attack().getTime(sampleId), 1);
//            else currentReduction = max(currentReduction - 1/attack().getTime(sampleId), 1);
//
//            lastSample *= makeup().getSample(sampleId);
//        }
//        return lastSample;
//    }
//}
