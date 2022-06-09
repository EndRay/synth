module ui.gui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires java.sql;
    requires ibatis.core;

    opens ui.gui to javafx.fxml;
    exports synthesizer.sources.utils;
    exports ui.gui;

    opens ui.gui.knob to javafx.fxml;

}