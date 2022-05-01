package database;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.*;
import java.sql.*;

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

    public static void main(String... args) {
    }
}
