import database.Database;
import ui.gui.MainGUI;

public class Launcher {
    static public void main(String... args){
        Database.main(args);
        MainGUI.main(args);
    }
}
