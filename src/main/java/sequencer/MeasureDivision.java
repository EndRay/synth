package sequencer;

import static sequencer.Clock.PPQ;

public enum MeasureDivision {
    WHOLE(4 * PPQ, "1/1"),
    HALF(2 * PPQ, "1/2"),
    QUARTER(PPQ, "1/4"),
    EIGHTH(PPQ / 2, "1/8"),
    SIXTEENTH(PPQ / 4, "1/16"),
    THIRTY_SECOND(PPQ / 8, "1/32"),

    HALF_TRIPLET((2 * PPQ) * 2 / 3, "1/2T"),
    QUARTER_TRIPLET((PPQ) * 2 / 3, "1/4T"),
    EIGHTH_TRIPLET((PPQ / 2) * 2 / 3, "1/8T"),
    SIXTEENTH_TRIPLET((PPQ / 4) * 2 / 3, "1/16T"),
    THIRTY_SECOND_TRIPLET((PPQ / 8) * 2 / 3, "1/32T"),

    HALF_DOTTED((2 * PPQ) * 3 / 2, "1/2D"),
    QUARTER_DOTTED((PPQ) * 3 / 2, "1/4D"),
    EIGHTH_DOTTED((PPQ / 2) * 3 / 2, "1/8D"),
    SIXTEENTH_DOTTED((PPQ / 4) * 3 / 2, "1/16D");

    private final int pings;
    private final String shortName;

    MeasureDivision(int pings, String shortName) {
        this.pings = pings;
        this.shortName = shortName;
    }

    public int getPings() {
        return pings;
    }

    public String getShortName() {
        return shortName;
    }
}
