package ui.gui.draggable;

import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class DraggablesUtils {
    private DraggablesUtils(){}

    static class DraggableController{
        boolean canDrag = false;
        boolean isDragged = false;
        double startX, startY;

        TitledPane pane;

        DraggableController(TitledPane pane){
            this.pane = pane;
        }

        void onPressed(MouseEvent e){
            if(e.getButton().equals(MouseButton.PRIMARY)) {
                startX = e.getX();
                startY = e.getY();
                canDrag = true;
            }
        }
        void onDragged(MouseEvent e){
            if(canDrag) {
                isDragged = true;
                double dx = e.getX() - startX;
                double dy = e.getY() - startY;
                pane.setTranslateX(pane.getTranslateX() + dx);
                pane.setTranslateY(pane.getTranslateY() + dy);
            }
        }
        void onReleased(MouseEvent e){
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
    }

    static public void makeDraggable(TitledPane pane, Node grip){
        DraggableController controller = new DraggableController(pane);

        grip.setOnMousePressed(controller::onPressed);
        grip.setOnMouseDragged(controller::onDragged);
        grip.setOnMouseReleased(controller::onReleased);

        pane.setCollapsible(false);
    }
}
