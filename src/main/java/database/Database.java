package database;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.util.stream.Stream;

public final class Database {

    static {
        init();
    }

    private Database() {
    }

    static Connection getConnection() {
        try {
            Connection c = DriverManager.getConnection("jdbc:sqlite:saves.sqlite");
            c.setAutoCommit(false);
            return c;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static void executeScript(File file) {
        try (Connection c = getConnection(); Reader reader = new BufferedReader(new FileReader(file))) {
            ScriptRunner scriptRunner = new ScriptRunner(c);
            scriptRunner.setLogWriter(null);
            scriptRunner.runScript(reader);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void drop() {
        executeScript(new File("src/main/java/database/drop.sql"));
    }

    public static void init() {
        executeScript(new File("src/main/java/database/init.sql"));
    }

    public static String getSynthStructure(String name) {
        try (Connection c = getConnection()) {
            Statement statement = c.createStatement();
            return statement.executeQuery("SELECT * FROM synths WHERE name == '" + name + "';").getString("structure");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveSynth(String name, String structure){
        try (Connection c = getConnection()) {
            Statement statement = c.createStatement();
            statement.executeUpdate("INSERT OR REPLACE INTO synths(name, structure) VALUES ('" + name + "', '" + structure + "')");
            c.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private final static File demoSynthsFolder = new File("demoSynths");

    public static void loadSynthsFromFolder(File folder, String namePrefix){
        File[] files = folder.listFiles();
        if(files == null)
            return;
        Stream.of(files).forEach(file -> {
            if(file.isDirectory())
                loadSynthsFromFolder(file, namePrefix + file.getName() + "/");
            if(!file.getName().endsWith(".patch"))
                return;
            try(Scanner scanner = new Scanner(file)) {
                StringBuilder structure = new StringBuilder();
                while(scanner.hasNextLine())
                    structure.append(scanner.nextLine()).append("\n");
                String name = file.getName();
                name = name.substring(0, name.length()-6);
                saveSynth(namePrefix + name, structure.toString());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void loadSynthsFromFolder(File folder){
        loadSynthsFromFolder(folder, "");
    }

    public static void main(String... args) {
        loadSynthsFromFolder(demoSynthsFolder);
        saveSynth("second test", "second test structure");
        saveSynth("test", "overwritten structure");
        System.out.println(getSynthStructure("test"));
        System.out.println(getSynthStructure("second test"));
    }
}
