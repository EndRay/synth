package ui.gui.synthblock;

import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class SynthBlockController {

    @FXML TitledPane pane;

    private boolean canDrag = false;
    private boolean isDragged = false;

    private double startX, startY;

    @FXML void onPressed(MouseEvent e){
        if(e.getButton().equals(MouseButton.PRIMARY)) {
            startX = e.getX();
            startY = e.getY();
            canDrag = true;
        }
    }
    @FXML void onDragged(MouseEvent e){
        if(canDrag) {
            isDragged = true;
            double dx = e.getX() - startX;
            double dy = e.getY() - startY;
            pane.setTranslateX(pane.getTranslateX() + dx);
            pane.setTranslateY(pane.getTranslateY() + dy);
        }
    }
    @FXML void onReleased(MouseEvent e){
        if(e.getButton().equals(MouseButton.PRIMARY)) {
            if (!isDragged) {
                pane.setCollapsible(true);
                pane.setExpanded(!pane.isExpanded());
                pane.setCollapsible(false);
            }
            isDragged = false;
            canDrag = false;
        }
    }

    @FXML void initialize(){
        pane.setCollapsible(false);
    }
}
