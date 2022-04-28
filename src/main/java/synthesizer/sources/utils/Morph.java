package synthesizer.sources.utils;

import synthesizer.sources.SignalSource;

import static java.lang.Math.abs;

public class Morph extends Mixer implements PseudoSocket{

    private Socket morph = new Socket();

    public Morph(){ super(0); }
    //public UnityMixer(int size){ super(size); }
    public Morph(SignalSource... sources) {
        super(sources);
    }

    protected Morph(Morph morph){
        super(morph);
        this.morph = morph.morph;
    }

    public Socket morph(){
        return morph;
    }

    @Override
    protected Morph copy(){
        return new Morph(this);
    }

    @Override
    protected double recalculate(int sampleId) {
        double pos = morph().getSample(sampleId) * (size()-1);
        double res = 0;
        for(int i = 0; i < size(); ++i) {
            double sample = get(i).getSample(sampleId);
            double coef = 1 - abs(i - pos);
            if(coef > 0)
                res += sample * coef;
        }
        return res;
    }
}
