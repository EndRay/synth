package synthesizer.sources.utils;

public class KnobSource extends SourceValue {
    String description;
    private final int x, y, size;

    public KnobSource(String name, double initialValue, String description, double x, double y, double size) {
        super(name, initialValue);
        if (size <= 0 || size % 1 != 0)
            throw new RuntimeException("knob size must be positive integer");
        if (x < 0 || x + size > 25 || x % 1 != 0)
            throw new RuntimeException("knob x coordinate must be an integer in [0, 25) interval");
        if (y < 0 || y + size > 15 || y % 1 != 0)
            throw new RuntimeException("knob y coordinate must be an integer in [0, 15) interval");
        this.description = description;
        this.x = (int) x;
        this.y = (int) y;
        this.size = (int) size;
    }

    public synchronized String getDescription() {
        return description;
    }

    public synchronized void setDescription(String description) {
        this.description = description;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

}
