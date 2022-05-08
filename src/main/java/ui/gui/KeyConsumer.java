package ui.gui;

import javafx.scene.input.KeyCode;

public interface KeyConsumer {
    void keyPressConsume(KeyCode key);
    void keyReleaseConsume(KeyCode key);
    void unfocus();
}
