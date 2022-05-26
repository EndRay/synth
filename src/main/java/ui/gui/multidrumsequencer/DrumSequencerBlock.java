package ui.gui.multidrumsequencer;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import midi.MidiUtils;
import sequencer.Clockable;
import ui.gui.draggable.Deletable;
import ui.gui.sequencer.ControlButton;

import javax.sound.midi.Receiver;

import static ui.gui.draggable.DraggablesUtils.makeDraggable;

public class DrumSequencerBlock extends TitledPane implements Deletable, Clockable {

    final DrumSequencerBlockController drumSequencerBlockController;
    final Label label;
    final Pane sequencersListPane;
    final Button addDrumSequencerButton;

    public DrumSequencerBlock(Receiver receiver){
        drumSequencerBlockController = new DrumSequencerBlockController(receiver);
        label = new Label("drum sequencer");
        label.minWidthProperty().bind(this.widthProperty());
        this.setGraphic(label);
        this.setMaxWidth(USE_PREF_SIZE);

        {
            VBox box = new VBox();
            box.setAlignment(Pos.CENTER);
            sequencersListPane = box;
        }

        drumSequencerBlockController.sequencersListPane = sequencersListPane;

        {
            addDrumSequencerButton = new ControlButton("+");
            addDrumSequencerButton.setOnAction(e -> drumSequencerBlockController.addDrumSequencer());
            sequencersListPane.getChildren().add(addDrumSequencerButton);

            drumSequencerBlockController.addDrumSequencerButton = addDrumSequencerButton;
        }

        this.setContent(sequencersListPane);

        {
            ToggleGroup group = new ToggleGroup();

            ContextMenu menu = new ContextMenu();
            for (int i = 0; i < MidiUtils.channels; ++i) {
                int channel = i;
                RadioMenuItem item = new RadioMenuItem("midi channel " + (channel + 1));
                item.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue)
                        setChannel(channel);
                });
                item.setToggleGroup(group);
                menu.getItems().add(item);
            }
            {
                RadioMenuItem item = new RadioMenuItem("disabled");
                item.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue)
                        setChannel(-1);
                });
                menu.getItems().add(item);
                item.setToggleGroup(group);
                item.setSelected(true);
            }
            label.setContextMenu(menu);
        }

        makeDraggable(this, label);

        drumSequencerBlockController.drumSequencerBlock = this;
        drumSequencerBlockController.initialize();
    }

    public void setChannel(int channel){
        drumSequencerBlockController.setChannel(channel);
    }

    @Override
    public void ping() {
        drumSequencerBlockController.ping();
    }

    @Override
    public void start() {
        drumSequencerBlockController.start();
    }

    @Override
    public void stop() {
        drumSequencerBlockController.stop();
    }

    @Override
    public void onDelete() {

    }
}
