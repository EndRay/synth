package structscript.polyphony;

public class Polyphonic implements PolyphonyType{
    private final int voicesCount;
    public Polyphonic(int voicesCount){
        if(voicesCount <= 0)
            throw new IllegalArgumentException("Voices amount must be positive.");
        this.voicesCount = voicesCount;
    }
    public int getVoicesCount(){
        return voicesCount;
    }

    @Override
    public String getShortName() {
        return "poly " + voicesCount;
    }
}
