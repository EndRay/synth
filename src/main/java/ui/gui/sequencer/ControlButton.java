package ui.gui.sequencer;


import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ControlButton extends Button {

    public static double buttonSize = 25;

    public ControlButton(String text){
        setText(String.valueOf(text));
        setPadding(new Insets(0, 4, 0, 4));
        setFont(Font.font("Monospaced", FontWeight.BOLD, buttonSize / 1.5));
        setMinHeight(USE_PREF_SIZE);
        setPrefHeight(buttonSize);
        setMaxHeight(USE_PREF_SIZE);
        minWidthProperty().bind(heightProperty());
    }
}
