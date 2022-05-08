package ui.gui.keyboardkey;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import ui.gui.keyboardblock.KeyboardBlock;

public class KeyboardKey extends Rectangle {

    public final static double keyWidth = 40;
    public final static double keyHeight = 80;

    final KeyboardKeyController keyboardKeyController;

    public KeyboardKey(KeyboardBlock keyboardBlock, int shift) {
        keyboardKeyController = new KeyboardKeyController();
        keyboardKeyController.shift = shift;
        keyboardKeyController.keyboardBlock = keyboardBlock;

        this.setWidth(keyWidth);
        this.setHeight(keyHeight);

        this.setArcWidth(keyWidth/2);
        this.setArcHeight(keyWidth/2);

        switch ((shift % 12 + 12) % 12) {
            case 0, 2, 4, 5, 7, 9, 11 -> this.setFill(Color.WHITE);
            default -> this.setFill(Color.BLACK);
        }

        this.setOnMousePressed(keyboardKeyController::OnPressed);
        this.setOnMouseReleased(keyboardKeyController::OnReleased);
    }
}
