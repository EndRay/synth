package database;

import javafx.beans.property.StringProperty;
import org.apache.ibatis.jdbc.ScriptRunner;

import javax.print.DocFlavor;
import java.io.*;
import java.sql.*;
import java.util.*;
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

    private static final String SQL_SYNTH_GET = "SELECT * FROM synths WHERE synth_name == ?";

    public static String getSynthStructure(String name) throws NoSuchSynthException {
        try (Connection c = getConnection();
             PreparedStatement statement = c.prepareStatement(SQL_SYNTH_GET)) {
            statement.setString(1, name);
            return statement.executeQuery().getString("structure");
        } catch (SQLException e) {
            throw new NoSuchSynthException();
        }
    }

    private static final String SQL_SYNTH_SAVE = "INSERT OR REPLACE INTO synths(synth_id, synth_name, structure) VALUES " +
            "((SELECT synth_id FROM synths WHERE synth_name = ?), ?, ?)";

    public static void saveSynth(String name, String structure) {
        try (Connection c = getConnection();
             PreparedStatement statement = c.prepareStatement(SQL_SYNTH_SAVE)) {
            statement.setString(1, name);
            statement.setString(2, name);
            statement.setString(3, structure);
            statement.executeUpdate();
            c.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private static final String SQL_GET_SYNTHS =
            "SELECT synth_name FROM synths";

    public static List<String> getSynths() {
        try (Connection c = getConnection();
             PreparedStatement st = c.prepareStatement(SQL_GET_SYNTHS)) {
            ResultSet synthsSet = st.executeQuery();
            List<String> synths = new ArrayList<>();
            while(synthsSet.next())
                synths.add(synthsSet.getString(1));
            return synths;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String SQL_PATCH_ID_GET = "SELECT patch_id FROM patches WHERE synth_id = (SELECT synth_id FROM synths WHERE synth_name = ?) AND patch_name = ?";
    private static final String SQL_PATCH_GET = "SELECT parameter_name, value FROM parameters WHERE patch_id = ?";

    public static Map<String, Double> getPatch(String synth, String patch) throws NoSuchPatchException {
        try (Connection c = getConnection();
             PreparedStatement patchIdStatement = c.prepareStatement(SQL_PATCH_ID_GET);
             PreparedStatement statement = c.prepareStatement(SQL_PATCH_GET)) {
            patchIdStatement.setString(1, synth);
            patchIdStatement.setString(2, patch);
            int patchId = patchIdStatement.executeQuery().getInt(1);
            statement.setInt(1, patchId);
            ResultSet res = statement.executeQuery();
            Map<String, Double> ans = new HashMap<>();
            while (res.next())
                ans.put(res.getString(1), res.getDouble(2));
            return ans;
        } catch (SQLException e) {
            throw new NoSuchPatchException();
        }
    }

    private static final String SQL_PATCH_CREATE =
            "INSERT OR IGNORE INTO patches (synth_id, patch_name) VALUES ((SELECT synth_id FROM synths WHERE synth_name = ?), ?)";
    private static final String SQL_PARAMETER_SAVE =
            "INSERT OR REPLACE INTO parameters (patch_id, parameter_name, value) VALUES (?, ?, ?)";

    public static void savePatch(String synth, String patch, Map<String, Double> parameters) throws NoSuchSynthException {
        try (Connection c = getConnection();
             PreparedStatement patchStatement = c.prepareStatement(SQL_PATCH_CREATE);
             PreparedStatement patchIdStatement = c.prepareStatement(SQL_PATCH_ID_GET)) {
            patchStatement.setString(1, synth);
            patchStatement.setString(2, patch);
            patchStatement.executeUpdate();
            patchIdStatement.setString(1, synth);
            patchIdStatement.setString(2, patch);
            int patchId = patchIdStatement.executeQuery().getInt(1);
            for (Map.Entry<String, Double> parameter : parameters.entrySet()) {
                try (PreparedStatement parameterStatement = c.prepareStatement(SQL_PARAMETER_SAVE)) {
                    parameterStatement.setInt(1, patchId);
                    parameterStatement.setString(2, parameter.getKey());
                    parameterStatement.setDouble(3, parameter.getValue());
                    parameterStatement.executeUpdate();
                }
            }
            c.commit();
        } catch (SQLException e) {
            throw new NoSuchSynthException();
        }
    }

    private final static File demoSynthsFolder = new File("demoSynths");

    public static void loadSynthsFromFolder(File folder, String namePrefix) {
        File[] files = folder.listFiles();
        if (files == null)
            return;
        Stream.of(files).forEach(file -> {
            if (file.isDirectory())
                loadSynthsFromFolder(file, namePrefix + file.getName() + "/");
            if (!file.getName().endsWith(".patch"))
                return;
            try (Scanner scanner = new Scanner(file)) {
                StringBuilder structure = new StringBuilder();
                while (scanner.hasNextLine())
                    structure.append(scanner.nextLine()).append("\n");
                String name = file.getName();
                name = name.substring(0, name.length() - 6);
                saveSynth(namePrefix + name, structure.toString());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void loadSynthsFromFolder(File folder) {
        loadSynthsFromFolder(folder, "");
    }

    public static void main(String... args) {
        loadSynthsFromFolder(demoSynthsFolder);
    }
}
