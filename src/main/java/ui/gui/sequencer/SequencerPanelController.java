package ui.gui.sequencer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import sequencer.MeasureDivision;
import sequencer.Sequence;
import sequencer.Sequencer;
import sequencer.Step;

import javax.sound.midi.Receiver;

public class SequencerPanelController{
    public static final Double defaultDefaultGate = 0.5;
    public final Sequencer sequencer;
    public final SequenceFX sequenceFX;
    private MeasureDivision nowMeasureDivision = MeasureDivision.QUARTER;
    public final BooleanProperty recording = new SimpleBooleanProperty(false);
    public final BooleanProperty muted = new SimpleBooleanProperty(false);
    protected Sequence newSequence = null;
    private Double nowDefaultGate = defaultDefaultGate;

    public SequencerPanelController(Receiver receiver){
        sequencer = new Sequencer(receiver, -1);
        sequenceFX = new SequenceFX();
    }
    public ReadOnlyBooleanProperty recordingProperty() {
        return recording;
    }
    public boolean isRecording(){
        return recordingProperty().get();
    }
    public ReadOnlyBooleanProperty mutedProperty() {
        return muted;
    }
    public boolean isMuted(){
        return mutedProperty().get();
    }

    @FXML
    public void onRecord(ActionEvent event){
        recording.setValue(recording.not().getValue());
    }

    public ChangeListener<String> divisionComboBoxListener = (observableValue, oldValue, newValue) -> {
        for(MeasureDivision our : MeasureDivision.values())
            if(our.getShortName().equals(newValue))
                nowMeasureDivision = our;
        if(newSequence != null)
            newSequence.setMeasureDivision(nowMeasureDivision);
    };

    public ChangeListener<Double> gateSpinnerListener = (observableValue, oldValue, newValue) -> {
        nowDefaultGate = newValue;
        if(newSequence != null)
            newSequence.setDefaultGate(newValue);
    };

    @FXML
    public void onTie(ActionEvent event){
        if(isRecording()) {
            newSequence.addStep(new Step());
            sequenceFX.updateProperties();
        }
    }

    @FXML
    public void onMute(ActionEvent event){
        muted.setValue(muted.not().getValue());
    }

    public void setChannel(int channel){
        sequencer.setMidiChannel(channel);
    }

    @FXML
    public void initialize() {
        recordingProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                newSequence = new Sequence(nowMeasureDivision);
                newSequence.setDefaultGate(nowDefaultGate);
                sequenceFX.setSequence(newSequence);
                sequenceFX.updateProperties();
            }else{
                sequencer.setSequence(newSequence);
            }
        });
        mutedProperty().addListener((observable, oldValue, newValue) -> sequencer.setMuted(newValue));
    }

    public void addStepOnPress(Step step){
        if(isRecording()){
            this.newSequence.addStep(step);
            this.sequenceFX.updateProperties();
        }
    }

    public void setMidiChannel(int channel) {
        sequencer.setMidiChannel(channel);
    }
}
