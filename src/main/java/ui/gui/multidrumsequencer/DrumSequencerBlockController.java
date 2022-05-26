package ui.gui.multidrumsequencer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import sequencer.Clockable;
import ui.gui.multidrumsequencer.drumsequencer.DrumSequencer;

import javax.sound.midi.Receiver;

public class DrumSequencerBlockController implements Clockable {

    Receiver receiver;
    DrumSequencerBlock drumSequencerBlock;

    int channel = -1;

    @FXML Pane sequencersListPane;
    @FXML Button addDrumSequencerButton;

    ObservableList<DrumSequencer> sequencersList = FXCollections.observableArrayList();

    DrumSequencerBlockController(Receiver receiver) {
        this.receiver = receiver;
    }

    @FXML
    void initialize(){
        addDrumSequencer();
    }

    @FXML
    void addDrumSequencer(){
        DrumSequencer drumSequencer = new DrumSequencer(receiver, channel);
        drumSequencer.setChannel(channel);
        sequencersList.add(drumSequencer);
        sequencersListPane.getChildren().add(drumSequencer);
        addDrumSequencerButton.toFront();
    }

    public void setChannel(int channel){
        this.channel = channel;
        for(DrumSequencer drumSequencer : sequencersList)
            drumSequencer.setChannel(channel);
    }

    @Override
    public void ping() {
        for(DrumSequencer drumSequencer : sequencersList)
            drumSequencer.ping();
    }

    @Override
    public void start() {
        for(DrumSequencer drumSequencer : sequencersList)
            drumSequencer.start();
    }

    @Override
    public void stop() {
        for(DrumSequencer drumSequencer : sequencersList)
            drumSequencer.stop();
    }
}
