package agendoc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

/**
 * Manages loading and saving of {@link Settings} to a JSON file
 * in the platform-appropriate user configuration directory.
 */
public class SettingsStorage
{

    private static final String CONFIG_DIR_NAME = "agendoc";
    private static final String SETTINGS_FILE_NAME = "settings.json";

    private final Gson gson;
    private final Path settingsFilePath;

    /**
     * Creates a SettingsStorage and determines the configuration file path.
     */
    public SettingsStorage()
    {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.settingsFilePath = resolveConfigDir().resolve(SETTINGS_FILE_NAME);
    }

    /**
     * Loads settings from the JSON configuration file.
     * Returns default settings if the file does not exist.
     *
     * @return the loaded or default settings
     */
    public Settings load()
    {
        if (!Files.exists(settingsFilePath))
        {
            return new Settings();
        }

        try (Reader reader = Files.newBufferedReader(settingsFilePath))
        {
            Settings settings = gson.fromJson(reader, Settings.class);
            if (settings == null)
            {
                return new Settings();
            }
            return settings;
        }
        catch (IOException e)
        {
            return new Settings();
        }
    }

    /**
     * Saves the given settings to the JSON configuration file.
     * Creates parent directories and sets restrictive file permissions on Linux.
     *
     * @param settings the settings to save
     * @throws IOException if an I/O error occurs
     */
    public void save(Settings settings) throws IOException
    {
        Path configDir = settingsFilePath.getParent();
        if (!Files.exists(configDir))
        {
            Files.createDirectories(configDir);
        }

        try (Writer writer = Files.newBufferedWriter(settingsFilePath))
        {
            gson.toJson(settings, writer);
        }

        if (!System.getProperty("os.name").toLowerCase().contains("win"))
        {
            Files.setPosixFilePermissions(settingsFilePath,
                PosixFilePermissions.fromString("rw-------"));
        }
    }

    /**
     * Resolves the platform-specific configuration directory.
     * On Windows uses %APPDATA%, on Linux uses $XDG_CONFIG_HOME or ~/.config.
     *
     * @return path to the agendoc configuration directory
     */
    private Path resolveConfigDir()
    {
        String os = System.getProperty("os.name").toLowerCase();
        String configBase;

        if (os.contains("win"))
        {
            configBase = System.getenv("APPDATA");
            if (configBase == null)
            {
                configBase = System.getProperty("user.home");
            }
        }
        else
        {
            configBase = System.getenv("XDG_CONFIG_HOME");
            if (configBase == null || configBase.isEmpty())
            {
                configBase = System.getProperty("user.home") + "/.config";
            }
        }

        return Paths.get(configBase, CONFIG_DIR_NAME);
    }
}