package midi;

public class MidiUtils {
    public static final int channels = 16;
    public static final int pitchbendRange = 128*128;
    public static final int lowestNote = 0;
    public static final int highestNote = 127;

    private MidiUtils(){}

    public static String getNoteName(int note){
        if(note < lowestNote || note > highestNote)
            throw new IllegalArgumentException();
        String letter;
        switch (note%12){
            case 0 -> letter = "C";
            case 1 -> letter = "C#";
            case 2 -> letter = "D";
            case 3 -> letter = "D#";
            case 4 -> letter = "E";
            case 5 -> letter = "F";
            case 6 -> letter = "F#";
            case 7 -> letter = "G";
            case 8 -> letter = "G#";
            case 9 -> letter = "A";
            case 10 -> letter = "A#";
            case 11 -> letter = "B";
            default -> throw new IllegalArgumentException();
        }
        return letter + (note/12 - 2);
    }
}
