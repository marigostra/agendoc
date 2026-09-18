package agendoc;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Main entry point for the Agendoc JavaFX application.
 */
public class AgendocApp extends Application
{

    @Override
    public void start(Stage primaryStage)
    {
        primaryStage.setTitle("Agendoc");

        Button llmButton = new Button("LLM");
        llmButton.setOnAction(e ->
        {
            SettingsForm settingsForm = new SettingsForm(primaryStage);
            settingsForm.showAndWait();
        });

        TextArea textArea = new TextArea();
        textArea.setPromptText("Enter multi-line text here...");
        textArea.setPrefRowCount(10);

        TextField textField = new TextField();
        textField.setPromptText("Enter text here...");

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll("Item 1", "Item 2", "Item 3");

        VBox vbox = new VBox(10, llmButton, textArea, textField, listView);
        vbox.setPadding(new Insets(10));

        Scene scene = new Scene(vbox, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Application entry point.
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }
}