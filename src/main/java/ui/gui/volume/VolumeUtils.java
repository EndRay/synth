package ui.gui.volume;

import javafx.scene.control.Slider;
import synthesizer.sources.utils.SourceValue;

import static java.lang.Math.exp;

public class VolumeUtils {
    private VolumeUtils(){}

    public static void makeVolumeSlider(Slider slider, SourceValue volume){
        slider.valueProperty().addListener((observable, oldValue, newValue) -> volume.setValue((exp(newValue.doubleValue())-1)/(Math.E - 1)));
        //slider.valueProperty().addListener((observable, oldValue, newValue) -> volume.setValue(newValue.doubleValue()));
    }
}
