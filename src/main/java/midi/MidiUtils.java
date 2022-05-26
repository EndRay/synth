package midi;

public class MidiUtils {
    public static final int channels = 16;
    public static final int pitchbendRange = 128*128;
    public static final int lowestNote = 0;
    public static final int highestNote = 127;

    private MidiUtils(){}

    public static int getNoteOctave(int note){
        return (note/12 - 2);
    }

    public static String getNoteLetter(int note){
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
        return letter;
    }

    public static String getNoteName(int note){
        if(note < lowestNote || note > highestNote)
            throw new IllegalArgumentException();
        return getNoteLetter(note) + getNoteOctave(note);
    }

    public static int getNoteByName(String name) {
        int note;
        switch (name.charAt(0)){
            case 'C' -> note = 0;
            case 'D' -> note = 2;
            case 'E' -> note = 3;
            case 'F' -> note = 5;
            case 'G' -> note = 7;
            case 'A' -> note = 9;
            case 'B' -> note = 11;
            default -> throw new IllegalArgumentException();
        }
        name = name.substring(1);
        while(name.charAt(0) == '#' || name.charAt(0) == 'b'){
            if(name.charAt(0) == '#')
                ++note;
            else --note;
            name = name.substring(1);
        }
        try{
            note += (Integer.parseInt(name) + 2) * 12;
        } catch (NumberFormatException e){
            throw new IllegalArgumentException();
        }
        if(note < lowestNote || note > highestNote)
            throw new IllegalArgumentException();
        return note;
    }
}
