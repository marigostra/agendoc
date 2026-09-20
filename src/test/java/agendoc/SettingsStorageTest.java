package agendoc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SettingsStorage}.
 */
class SettingsStorageTest
{

    @TempDir
    Path tempDir;

    private SettingsStorage storage;

    @BeforeEach
    void setUp()
    {
        storage = new SettingsStorage(tempDir);
    }

    @Test
    void loadShouldReturnDefaultSettingsWhenFileDoesNotExist()
    {
        Settings settings = storage.load();

        assertNotNull(settings);
        assertEquals("https://api.openai.com/v1", settings.getEndpoint());
        assertEquals("", settings.getToken());
        assertEquals("gpt-4", settings.getModel());
        assertEquals("", settings.getProject());
        assertEquals(30000, settings.getTimeout());
        assertEquals(10, settings.getToolCallLimit());
        assertEquals(0.7, settings.getTemperature(), 0.001);
    }

    @Test
    void saveAndLoadShouldPreserveAllFields() throws IOException
    {
        Settings original = new Settings();
        original.setEndpoint("https://custom.api.com/v2");
        original.setToken("sk-test-token-123");
        original.setModel("gpt-3.5-turbo");
        original.setProject("my-test-project");
        original.setTimeout(60000);
        original.setToolCallLimit(5);
        original.setTemperature(0.3);

        storage.save(original);

        Settings loaded = storage.load();

        assertEquals("https://custom.api.com/v2", loaded.getEndpoint());
        assertEquals("sk-test-token-123", loaded.getToken());
        assertEquals("gpt-3.5-turbo", loaded.getModel());
        assertEquals("my-test-project", loaded.getProject());
        assertEquals(60000, loaded.getTimeout());
        assertEquals(5, loaded.getToolCallLimit());
        assertEquals(0.3, loaded.getTemperature(), 0.001);
    }

    @Test
    void loadShouldReturnDefaultSettingsWhenFileIsEmpty() throws IOException
    {
        Path settingsFile = tempDir.resolve("settings.json");
        Files.createFile(settingsFile);

        Settings settings = storage.load();

        assertNotNull(settings);
        assertEquals("https://api.openai.com/v1", settings.getEndpoint());
    }

    @Test
    void loadShouldReturnDefaultSettingsWhenJsonIsInvalid() throws IOException
    {
        Path settingsFile = tempDir.resolve("settings.json");
        Files.writeString(settingsFile, "this is not valid json {{{");

        Settings settings = storage.load();

        assertNotNull(settings);
        assertEquals("https://api.openai.com/v1", settings.getEndpoint());
    }

    @Test
    void loadShouldReturnDefaultSettingsWhenJsonIsMalformed() throws IOException
    {
        Path settingsFile = tempDir.resolve("settings.json");
        Files.writeString(settingsFile, "{\"endpoint\": \"https://test.com\", \"timeout\": \"not_a_number\"}");

        Settings settings = storage.load();

        assertNotNull(settings);
        assertEquals("https://api.openai.com/v1", settings.getEndpoint());
    }

    @Test
    void saveShouldCreateParentDirectories() throws IOException
    {
        Settings settings = new Settings();
        settings.setEndpoint("https://nested.dir.test.com");

        storage.save(settings);

        Path settingsFile = tempDir.resolve("settings.json");
        assertTrue(Files.exists(settingsFile));

        String content = Files.readString(settingsFile);
        assertTrue(content.contains("https://nested.dir.test.com"));
    }

    @Test
    void saveShouldOverwriteExistingFile() throws IOException
    {
        Settings first = new Settings();
        first.setEndpoint("https://first.com");
        storage.save(first);

        Settings second = new Settings();
        second.setEndpoint("https://second.com");
        storage.save(second);

        Settings loaded = storage.load();
        assertEquals("https://second.com", loaded.getEndpoint());
    }

    @Test
    void loadShouldHandlePartialJsonGracefully() throws IOException
    {
        Path settingsFile = tempDir.resolve("settings.json");
        Files.writeString(settingsFile, "{\"endpoint\": \"https://partial.com\", \"model\": \"custom-model\"}");

        Settings settings = storage.load();

        assertNotNull(settings);
        assertEquals("https://partial.com", settings.getEndpoint());
        assertEquals("custom-model", settings.getModel());
        // Fields not present in JSON should retain default values
        assertEquals(30000, settings.getTimeout());
        assertEquals(0.7, settings.getTemperature(), 0.001);
    }

    @Test
    void saveShouldHandleEmptyToken() throws IOException
    {
        Settings settings = new Settings();
        settings.setToken("");

        storage.save(settings);

        Settings loaded = storage.load();
        assertEquals("", loaded.getToken());
    }

    @Test
    void saveShouldHandleSpecialCharactersInFields() throws IOException
    {
        Settings settings = new Settings();
        settings.setEndpoint("https://api.example.com/path?query=value&foo=bar");
        settings.setToken("sk-token-with-special-chars-!@#$%^&*()");
        settings.setProject("project/with/slashes");

        storage.save(settings);

        Settings loaded = storage.load();
        assertEquals("https://api.example.com/path?query=value&foo=bar", loaded.getEndpoint());
        assertEquals("sk-token-with-special-chars-!@#$%^&*()", loaded.getToken());
        assertEquals("project/with/slashes", loaded.getProject());
    }

    @Test
    void loadShouldReturnNewInstanceEachTime() throws IOException
    {
        Settings settings = new Settings();
        settings.setEndpoint("https://instance-test.com");
        storage.save(settings);

        Settings first = storage.load();
        Settings second = storage.load();

        assertNotSame(first, second);
        assertEquals(first.getEndpoint(), second.getEndpoint());
    }
}