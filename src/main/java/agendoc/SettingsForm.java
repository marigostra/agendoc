package agendoc;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Modal dialog form for editing OpenAI API access settings.
 */
public class SettingsForm extends Stage
{

    private final SettingsStorage storage;

    private TextField endpointField;
    private PasswordField tokenField;
    private TextField modelField;
    private TextField projectField;
    private TextField timeoutField;
    private TextField toolCallLimitField;
    private TextField temperatureField;

    /**
     * Constructs a modal Settings form dialog owned by the given stage.
     *
     * @param owner the parent stage
     */
    public SettingsForm(Stage owner)
    {
        this.storage = new SettingsStorage();

        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        setTitle("Настройки");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        Label endpointLabel = new Label("Адрес для подключения:");
        endpointField = new TextField();
        endpointField.setPromptText("https://api.openai.com/v1");
        grid.add(endpointLabel, 0, 0);
        grid.add(endpointField, 1, 0);

        Label tokenLabel = new Label("Токен доступа:");
        tokenField = new PasswordField();
        tokenField.setPromptText("sk-...");
        grid.add(tokenLabel, 0, 1);
        grid.add(tokenField, 1, 1);

        Label modelLabel = new Label("Название модели:");
        modelField = new TextField();
        modelField.setPromptText("gpt-4");
        grid.add(modelLabel, 0, 2);
        grid.add(modelField, 1, 2);

        Label projectLabel = new Label("Проект:");
        projectField = new TextField();
        projectField.setPromptText("my-project");
        grid.add(projectLabel, 0, 3);
        grid.add(projectField, 1, 3);

        Label timeoutLabel = new Label("Таймаут (мс):");
        timeoutField = new TextField();
        timeoutField.setPromptText("30000");
        grid.add(timeoutLabel, 0, 4);
        grid.add(timeoutField, 1, 4);

        Label toolCallLimitLabel = new Label("Лимит вызовов инструментов:");
        toolCallLimitField = new TextField();
        toolCallLimitField.setPromptText("10");
        grid.add(toolCallLimitLabel, 0, 5);
        grid.add(toolCallLimitField, 1, 5);

        Label temperatureLabel = new Label("Температура:");
        temperatureField = new TextField();
        temperatureField.setPromptText("0.7");
        grid.add(temperatureLabel, 0, 6);
        grid.add(temperatureField, 1, 6);

        Button saveButton = new Button("Сохранить");
        saveButton.setOnAction(e ->
        {
            saveSettings();
            close();
        });

        Button cancelButton = new Button("Отмена");
        cancelButton.setOnAction(e -> close());

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        VBox root = new VBox(10, grid, buttonBox);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 450, 350);
        setScene(scene);

        loadSettings();
    }

    /**
     * Loads saved settings and populates the form fields.
     */
    private void loadSettings()
    {
        Settings settings = storage.load();
        endpointField.setText(settings.getEndpoint());
        tokenField.setText(settings.getToken());
        modelField.setText(settings.getModel());
        projectField.setText(settings.getProject());
        timeoutField.setText(String.valueOf(settings.getTimeout()));
        toolCallLimitField.setText(String.valueOf(settings.getToolCallLimit()));
        temperatureField.setText(String.valueOf(settings.getTemperature()));
    }

    /**
     * Collects values from form fields and saves them via SettingsStorage.
     */
    private void saveSettings()
    {
        Settings settings = new Settings();
        settings.setEndpoint(endpointField.getText());
        settings.setToken(tokenField.getText());
        settings.setModel(modelField.getText());
        settings.setProject(projectField.getText());

        try
        {
            settings.setTimeout(Integer.parseInt(timeoutField.getText()));
        }
        catch (NumberFormatException e)
        {
            settings.setTimeout(30000);
        }

        try
        {
            settings.setToolCallLimit(Integer.parseInt(toolCallLimitField.getText()));
        }
        catch (NumberFormatException e)
        {
            settings.setToolCallLimit(10);
        }

        try
        {
            settings.setTemperature(Double.parseDouble(temperatureField.getText()));
        }
        catch (NumberFormatException e)
        {
            settings.setTemperature(0.7);
        }

        try
        {
            storage.save(settings);
        }
        catch (IOException e)
        {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }
}