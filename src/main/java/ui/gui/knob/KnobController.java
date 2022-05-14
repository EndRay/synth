package ui.gui.knob;

import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import static javafx.beans.binding.Bindings.min;

public class KnobController {

    private static final double knobSizeCoefficient = 0.9;
    private static final double textSizeCoefficient = 0.12;
    private static final double dragSpeed = 0.004;
    private static final double scrollSpeed = 0.00015;

    DoubleProperty sizeProperty;

    private Slider slider;

    @FXML Label text;
    @FXML StackPane pane;
    @FXML ImageView sprite;

    private double prevDragY;

    @FXML void OnKnobPressed(MouseEvent event){
        prevDragY = event.getSceneY();
        setValueChanging(true);
    }
    @FXML void OnKnobDragged(MouseEvent event){
        double dy = event.getSceneY() - prevDragY;
        prevDragY = event.getSceneY();
        double min = getMin();
        double max = getMax();
        double val = getValue();
        val += - dy * dragSpeed * (max - min);
        setValue(val);
    }
    @FXML void OnKnobReleased(MouseEvent event){
        setValueChanging(false);
    }
    @FXML void OnKnobScrolled(ScrollEvent event){
        double dy = event.getDeltaY();
        double min = getMin();
        double max = getMax();
        double val = getValue();
        val += dy * scrollSpeed * (max - min);
        setValue(val);
        event.consume();
    }

    public void initialize(){
        sprite.fitWidthProperty().bind(pane.widthProperty().multiply(knobSizeCoefficient));
        sprite.fitHeightProperty().bind(pane.heightProperty().multiply(knobSizeCoefficient));

        sizeProperty = new SimpleDoubleProperty();
        sizeProperty.addListener(((observable, oldValue, newValue) -> text.setFont(Font.font("Comic Sans MS", newValue.doubleValue()*textSizeCoefficient))));
        sizeProperty.bind(min(pane.widthProperty(), pane.heightProperty()));
        slider = new Slider(0, 1, 0.5);

        valueProperty().addListener((observableValue, oldValue, newValue) -> {
            double min = getMin();
            double max = getMax();
            double val = newValue.doubleValue();
            sprite.rotateProperty().set((val - min) / max * 270 - 135);
        });
    }

    public final double getSize(){
        return sizeProperty().doubleValue();
    }
    public final ReadOnlyDoubleProperty sizeProperty(){
        return sizeProperty;
    }

    public final double getMin() {
        return slider.getMin();
    }

    public final void setMin(double min) {
        slider.setMin(min);
    }

    public final DoubleProperty minProperty() {
        return slider.minProperty();
    }

    public final double getMax() {
        return slider.getMax();
    }

    public final void setMax(double max) {
        slider.setMax(max);
    }

    public final DoubleProperty maxProperty() {
        return slider.maxProperty();
    }

    public final double getValue() {
        return slider.getValue();
    }

    public final void setValue(double value) {
        slider.setValue(value);
    }

    public final DoubleProperty valueProperty() {
        return slider.valueProperty();
    }

    public final boolean isValueChanging() {
        return slider.isValueChanging();
    }

    public final void setValueChanging(boolean b) {
        slider.setValueChanging(b);
    }

    public final BooleanProperty valueChangingProperty() {
        return slider.valueChangingProperty();
    }

    public final String getText(){
        return text.getText();
    }
    public final void setText(String description){
        text.setText(description);
    }
    public final StringProperty textProperty(){
        return text.textProperty();
    }
}
