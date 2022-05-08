package ui.gui.keyboardkey;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import ui.gui.keyboardblock.KeyboardBlock;

public class KeyboardKeyController {

    @FXML KeyboardBlock keyboardBlock;
    @FXML KeyboardKey keyboardKey;

    @FXML void OnPressed(MouseEvent mouseEvent){
        keyboardBlock.pressKey(keyboardKey);
    }
    @FXML void OnReleased(MouseEvent mouseEvent){
        keyboardBlock.releaseKey(keyboardKey);
    }
}
