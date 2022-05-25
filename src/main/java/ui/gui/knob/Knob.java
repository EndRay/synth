package ui.gui.knob;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Knob extends Region {

    final static Image image;
    final static Image fixedImage;

    static {
        try {
            image = new Image(Knob.class.getResource("knob.png").toURI().toString());
            URL fixedURL = Knob.class.getResource("fixedKnob.png");
            if(fixedURL == null)
                fixedImage = null;
            else fixedImage = new Image(fixedURL.toURI().toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    final KnobController controller;

    public Knob(){
        controller = new KnobController();

        ImageView sprite = new ImageView(image);
        ImageView fixedSprite = null;
        if(fixedImage != null)
             fixedSprite = new ImageView(fixedImage);
        Label text = new Label();
        text.setTextAlignment(TextAlignment.CENTER);
        VBox textBox = new VBox(text);
        textBox.setAlignment(Pos.BOTTOM_CENTER);
        StackPane spriteBox;
        if(fixedSprite == null)
            spriteBox = new StackPane(sprite);
        else spriteBox = new StackPane(fixedSprite, sprite);
        spriteBox.setAlignment(Pos.CENTER);
        StackPane pane = new StackPane(textBox, spriteBox);
        spriteBox.minWidthProperty().bind(pane.widthProperty());
        spriteBox.maxWidthProperty().bind(pane.widthProperty());
        spriteBox.minHeightProperty().bind(pane.heightProperty());
        spriteBox.maxHeightProperty().bind(pane.heightProperty());

        controller.sprite = sprite;
        controller.fixedSprite = fixedSprite;
        controller.text = text;
        controller.pane = pane;

        sprite.setOnMousePressed(controller::OnKnobPressed);
        sprite.setOnMouseDragged(controller::OnKnobDragged);
        sprite.setOnMouseReleased(controller::OnKnobReleased);
        sprite.setOnScroll(controller::OnKnobScrolled);

        if(fixedSprite != null) {
            fixedSprite.setOnMousePressed(controller::OnKnobPressed);
            fixedSprite.setOnMouseDragged(controller::OnKnobDragged);
            fixedSprite.setOnMouseReleased(controller::OnKnobReleased);
            fixedSprite.setOnScroll(controller::OnKnobScrolled);
        }

        controller.initialize();

        this.getChildren().add(pane);
        pane.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        pane.maxWidthProperty().bind(this.widthProperty());
        pane.maxHeightProperty().bind(this.heightProperty());
    }

    public final double getMin() {
        return controller.getMin();
    }

    public final void setMin(double min) {
        controller.setMin(min);
    }

    public final DoubleProperty minProperty() {
        return controller.minProperty();
    }

    public final double getMax() {
        return controller.getMax();
    }

    public final void setMax(double max) {
        controller.setMax(max);
    }

    public final DoubleProperty maxProperty() {
        return controller.maxProperty();
    }

    public final double getValue() {
        return controller.getValue();
    }

    public final void setValue(double value) {
        controller.setValue(value);
    }

    public final DoubleProperty valueProperty() {
        return controller.valueProperty();
    }

    public final boolean isValueChanging() {
        return controller.isValueChanging();
    }

    public final void setValueChanging(boolean b) {
        controller.setValueChanging(b);
    }

    public final BooleanProperty valueChangingProperty() {
        return controller.valueChangingProperty();
    }

    public final String getText(){
        return controller.getText();
    }
    public final void setText(String description){
        controller.setText(description);
    }
    public final StringProperty textProperty(){
        return controller.textProperty();
    }
}
