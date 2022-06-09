package database;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.*;
import java.net.URISyntaxException;
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

//    public static void drop() {
//        try {
//            executeScript(new File(Database.class.getResource("drop.sql").toURI()));
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static void init() {
        try (Connection c = getConnection(); Reader reader = new BufferedReader(new InputStreamReader(Database.class.getResourceAsStream("init.sql")))) {
            ScriptRunner scriptRunner = new ScriptRunner(c);
            scriptRunner.setLogWriter(null);
            scriptRunner.runScript(reader);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
//        try {
//            executeScript(new File(Database.class.getResource("init.sql").toURI()));
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
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
            while (synthsSet.next())
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

    private static final String SQL_PATCHES_GET =
            "SELECT patch_name FROM patches WHERE synth_id = (SELECT synth_id FROM synths WHERE synth_name = ?)";

    public static Collection<String> getPatches(String synth) {
        try (Connection c = getConnection();
             PreparedStatement statement = c.prepareStatement(SQL_PATCHES_GET)) {
            statement.setString(1, synth);
            ResultSet res = statement.executeQuery();
            List<String> ans = new ArrayList<>();
            while (res.next())
                ans.add(res.getString(1));
            return ans;
        } catch (SQLException e) {
            throw new RuntimeException();
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

    public static class Setup{
        final private Collection<Block> blocks;
        final private double BPM;

        public Setup(Collection<Block> blocks, double BPM){
            this.blocks = blocks;
            this.BPM = BPM;
        }

        public Collection<Block> blocks(){ return blocks; }
        public double BPM(){ return BPM; }
    }

    public static class Block {
        final private String type;
        final private int x;
        final private int y;

        public Block(String type, int x, int y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }

        public String type() {
            return type;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }
    }

    public static class SynthBlock extends Block {
        final private String synth;
        final private String patch;
        final private String polyphony;
        final private double volume;

        public SynthBlock(int x, int y, String synth, String patch, String polyphony, double volume) {
            super("synth", x, y);
            this.synth = synth;
            this.patch = patch;
            this.polyphony = polyphony;
            this.volume = volume;
        }

        public String synth() {
            return synth;
        }

        public String patch() {
            return patch;
        }

        public String polyphony(){
            return polyphony;
        }

        public double volume(){
            return volume;
        }
    }

    private static final String SQL_SETUPS_GET =
            "SELECT setup_name FROM setups";

    public static Collection<String> getSetups() {
        try (Connection c = getConnection();
             PreparedStatement setupsGetStatement = c.prepareStatement(SQL_SETUPS_GET)) {
            ResultSet res = setupsGetStatement.executeQuery();
            List<String> ans = new ArrayList<>();
            while(res.next())
                ans.add(res.getString(1));
            return ans;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String SQL_SETUP_ID_GET =
            "SELECT setup_id, bpm FROM setups WHERE setup_name = ?";
    private static final String SQL_SETUP_BLOCKS_GET =
            "SELECT block_id, block_type, x_pos, y_pos FROM blocks WHERE setup_id = ?";
    private static final String SQL_BLOCK_PATCH_GET =
            "SELECT synth_name, patch_name, polyphony, volume FROM synth_blocks_patches NATURAL JOIN synths LEFT JOIN patches ON synth_blocks_patches.patch_id = patches.patch_id WHERE block_id = ?";


    public static Setup getSetup(String name) throws NoSuchSetupException {
        try (Connection c = getConnection();
             PreparedStatement setupIdStatement = c.prepareStatement(SQL_SETUP_ID_GET);
             PreparedStatement setupBlocksStatement = c.prepareStatement(SQL_SETUP_BLOCKS_GET)) {
            List<Block> ans = new ArrayList<>();
            setupIdStatement.setString(1, name);
            ResultSet tmp = setupIdStatement.executeQuery();
            int id = tmp.getInt(1);
            double BPM = tmp.getDouble(2);
            setupBlocksStatement.setInt(1, id);
            ResultSet blocks = setupBlocksStatement.executeQuery();
            while (blocks.next()) {
                int blockId = blocks.getInt(1);
                String type = blocks.getString(2);
                int x = blocks.getInt(3);
                int y = blocks.getInt(4);
                if ("synth".equals(type)) {
                    try (PreparedStatement blockPatchStatement = c.prepareStatement(SQL_BLOCK_PATCH_GET)) {
                        blockPatchStatement.setInt(1, blockId);
                        ResultSet res = blockPatchStatement.executeQuery();
                        if(res.next())
                            ans.add(new SynthBlock(x, y, res.getString(1), res.getString(2), res.getString(3), res.getDouble(4)));
                        else throw new RuntimeException();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else ans.add(new Block(type, x, y));
            }
            return new Setup(ans, BPM);
        } catch (SQLException e) {
            throw new NoSuchSetupException();
        }
    }

    private static final String SQL_SETUP_CREATE =
            "INSERT OR REPLACE INTO setups(setup_name, bpm) VALUES (?, ?)";
    private static final String SQL_SETUP_BLOCKS_CLEAR =
            "DELETE FROM blocks WHERE setup_id = ?";
    private static final String SQL_BLOCK_INSERT =
            "INSERT INTO blocks(setup_id, block_type, x_pos, y_pos) VALUES (?, ?, ?, ?) RETURNING block_id";
    private static final String SQL_SYNTH_ID_GET =
            "SELECT synth_id FROM synths WHERE synth_name = ?";
    private static final String SQL_SYNTH_BLOCK_INSERT =
            "INSERT INTO synth_blocks_patches(block_id, synth_id, patch_id, polyphony, volume) VALUES (?, ?, ?, ?, ?)";


    public static void saveSetup(String name, double BPM, Collection<Block> blocks) {
        try (Connection c = getConnection();
             PreparedStatement setupCreateStatement = c.prepareStatement(SQL_SETUP_CREATE);
             PreparedStatement setupIdGetStatement = c.prepareStatement(SQL_SETUP_ID_GET);
             PreparedStatement setupBlocksClearStatement = c.prepareStatement(SQL_SETUP_BLOCKS_CLEAR)) {
            setupCreateStatement.setString(1, name);
            setupCreateStatement.setDouble(2, BPM);
            setupCreateStatement.executeUpdate();
            setupIdGetStatement.setString(1, name);
            int id = setupIdGetStatement.executeQuery().getInt(1);
            setupBlocksClearStatement.setInt(1, id);
            setupBlocksClearStatement.executeUpdate();
            for (Block block : blocks) {
                try (PreparedStatement blockInsertStatement = c.prepareStatement(SQL_BLOCK_INSERT);
                     PreparedStatement synthIdGetStatement = c.prepareStatement(SQL_SYNTH_ID_GET);
                     PreparedStatement patchIdGetStatement = c.prepareStatement(SQL_PATCH_ID_GET);
                     PreparedStatement synthBlockInsertStatement = c.prepareStatement(SQL_SYNTH_BLOCK_INSERT)) {
                    blockInsertStatement.setInt(1, id);
                    blockInsertStatement.setString(2, block.type());
                    blockInsertStatement.setInt(3, block.x());
                    blockInsertStatement.setInt(4, block.y());
                    int blockId = blockInsertStatement.executeQuery().getInt(1);
                    if (block instanceof SynthBlock synthBlock) {
                        synthIdGetStatement.setString(1, synthBlock.synth());
                        int synthId = synthIdGetStatement.executeQuery().getInt(1);
                        patchIdGetStatement.setString(1, synthBlock.synth());
                        patchIdGetStatement.setString(2, synthBlock.patch());
                        int patchId;
                        {
                            ResultSet res = patchIdGetStatement.executeQuery();
                            if(res.next())
                                patchId = res.getInt(1);
                            else patchId = 0;
                        }
                        synthBlockInsertStatement.setInt(1, blockId);
                        synthBlockInsertStatement.setInt(2, synthId);
                        if (patchId != 0)
                            synthBlockInsertStatement.setInt(3, patchId);
                        else synthBlockInsertStatement.setNull(3, Types.INTEGER);
                        synthBlockInsertStatement.setString(4, synthBlock.polyphony());
                        synthBlockInsertStatement.setDouble(5, synthBlock.volume());
                        synthBlockInsertStatement.executeUpdate();
                    }
                }
            }
            c.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
