package ui.gui.keyboardkey;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import ui.gui.keyboardblock.KeyboardBlock;

public class KeyboardKeyController {

    @FXML int shift;
    @FXML KeyboardBlock keyboardBlock;

    @FXML void OnPressed(MouseEvent mouseEvent){
        keyboardBlock.pressKey(shift);
    }
    @FXML void OnReleased(MouseEvent mouseEvent){
        keyboardBlock.releaseKey(shift);
    }
}
