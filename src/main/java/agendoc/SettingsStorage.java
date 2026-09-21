package agendoc;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Properties;

/**
 * Manages loading and saving of {@link Settings} to a properties file
 * in the platform-appropriate user configuration directory.
 */
public class SettingsStorage
{

    private static final String CONFIG_DIR_NAME = "agendoc";
    private static final String SETTINGS_FILE_NAME = "settings.properties";

    private static final String KEY_ENDPOINT = "endpoint";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_MODEL = "model";
    private static final String KEY_PROJECT = "project";
    private static final String KEY_TIMEOUT = "timeout";
    private static final String KEY_TOOL_CALL_LIMIT = "toolCallLimit";
    private static final String KEY_TEMPERATURE = "temperature";

    private final Path settingsFilePath;

    /**
     * Creates a SettingsStorage and determines the configuration file path
     * using the platform-specific config directory.
     */
    public SettingsStorage()
    {
        this(resolveConfigDir());
    }

    /**
     * Creates a SettingsStorage using the given directory as the config directory.
     * Intended for testing purposes.
     *
     * @param configDir the directory where the settings file will be stored
     */
    SettingsStorage(Path configDir)
    {
        this.settingsFilePath = configDir.resolve(SETTINGS_FILE_NAME);
    }

    /**
     * Loads settings from the properties configuration file.
     * Returns default settings if the file does not exist, is empty,
     * or contains unparseable values.
     *
     * @return the loaded or default settings
     */
    public Settings load()
    {
        if (!Files.exists(settingsFilePath))
            return new Settings();
        try (Reader reader = Files.newBufferedReader(settingsFilePath))
        {
            Properties props = new Properties();
            props.load(reader);
            return fromProperties(props);
        }
        catch (Exception e)
        {
            return new Settings();
        }
    }

    /**
     * Saves the given settings to the properties configuration file.
     * Creates parent directories and sets restrictive file permissions on Linux.
     *
     * @param settings the settings to save
     * @throws IOException if an I/O error occurs
     */
    public void save(Settings settings) throws IOException
    {
        Path configDir = settingsFilePath.getParent();
        if (!Files.exists(configDir))
            Files.createDirectories(configDir);
        Properties props = toProperties(settings);
        try (Writer writer = Files.newBufferedWriter(settingsFilePath))
        {
            props.store(writer, "Agendoc Settings");
        }
        if (!System.getProperty("os.name").toLowerCase().contains("win"))
        {
            try
            {
                Files.setPosixFilePermissions(settingsFilePath,
                    PosixFilePermissions.fromString("rw-------"));
            }
            catch (UnsupportedOperationException | IOException e)
            {
                // Permissions are best-effort; ignore if not supported by the file system
            }
        }
    }

    /**
     * Converts a {@link Properties} object to a {@link Settings} instance.
     * Missing keys retain default values; unparseable numbers are silently ignored.
     *
     * @param props the properties loaded from file
     * @return a populated Settings instance
     */
    private static Settings fromProperties(Properties props)
    {
        Settings settings = new Settings();
        if (props.containsKey(KEY_ENDPOINT))
            settings.setEndpoint(props.getProperty(KEY_ENDPOINT));
        if (props.containsKey(KEY_TOKEN))
            settings.setToken(props.getProperty(KEY_TOKEN));
        if (props.containsKey(KEY_MODEL))
            settings.setModel(props.getProperty(KEY_MODEL));
        if (props.containsKey(KEY_PROJECT))
            settings.setProject(props.getProperty(KEY_PROJECT));
        if (props.containsKey(KEY_TIMEOUT))
        {
            try
            {
                settings.setTimeoutSec(Integer.parseInt(props.getProperty(KEY_TIMEOUT)));
            }
            catch (NumberFormatException e)
            {
                // keep default
            }
        }
        if (props.containsKey(KEY_TOOL_CALL_LIMIT))
        {
            try
            {
                settings.setToolCallLimit(Integer.parseInt(props.getProperty(KEY_TOOL_CALL_LIMIT)));
            }
            catch (NumberFormatException e)
            {
                // keep default
            }
        }
        if (props.containsKey(KEY_TEMPERATURE))
        {
            try
            {
                settings.setTemperature(Double.parseDouble(props.getProperty(KEY_TEMPERATURE)));
            }
            catch (NumberFormatException e)
            {
                // keep default
            }
        }
        return settings;
    }

    /**
     * Converts a {@link Settings} instance to a {@link Properties} object.
     *
     * @param settings the settings to convert
     * @return a populated Properties instance
     */
    private static Properties toProperties(Settings settings)
    {
        Properties props = new Properties();
        props.setProperty(KEY_ENDPOINT, settings.getEndpoint());
        props.setProperty(KEY_TOKEN, settings.getToken());
        props.setProperty(KEY_MODEL, settings.getModel());
        props.setProperty(KEY_PROJECT, settings.getProject());
        props.setProperty(KEY_TIMEOUT, String.valueOf(settings.getTimeoutSec()));
        props.setProperty(KEY_TOOL_CALL_LIMIT, String.valueOf(settings.getToolCallLimit()));
        props.setProperty(KEY_TEMPERATURE, String.valueOf(settings.getTemperature()));
        return props;
    }

    /**
     * Resolves the platform-specific configuration directory.
     * On Windows uses %APPDATA%, on Linux uses $XDG_CONFIG_HOME or ~/.config.
     *
     * @return path to the agendoc configuration directory
     */
    private static Path resolveConfigDir()
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
