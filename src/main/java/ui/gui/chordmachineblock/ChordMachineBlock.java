package ui.gui.chordmachineblock;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import midi.MidiUtils;

import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import static ui.gui.draggable.DraggablesUtils.makeDraggable;

public class ChordMachineBlock extends TitledPane implements Transmitter {
    final Label label;

    final public static int width = 4;
    final public static int height = 2;

    Receiver receiver;

    int channel;

    public ChordMachineBlock(Receiver receiver){
        this.receiver = receiver;
        label = new Label("chord machine");
        label.minWidthProperty().bind(this.widthProperty());
        this.setGraphic(label);
        this.setMaxWidth(USE_PREF_SIZE);
        makeDraggable(this, label);
        {
            GridPane grid = new GridPane();
            for(int i = 0; i < width; ++i)
                for(int j = 0; j < height; ++j)
                    grid.add(new ChordKey(this), i, j);
            grid.setHgap(10);
            grid.setVgap(10);
            this.setContent(grid);
        }

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

    public void setChannel(int channel){
        this.channel = channel;
    }

    public int getChannel(){
        return channel;
    }

    @Override
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public Receiver getReceiver() {
        return receiver;
    }

    @Override
    public void close() {

    }
}
