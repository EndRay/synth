package Sources.Utils;

public class AC extends DC{
    public AC(){
        super(0);
    }

    @Override
    public void setOffset(double offset){
        if(offset != 0)
            throw new UnsupportedOperationException();
    }
}
