package agendoc;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
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

        VBox mainBox = new VBox(10, llmButton, textArea, textField);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        ColumnConstraints mainColumn = new ColumnConstraints();
        mainColumn.setPercentWidth(75);

        ColumnConstraints listColumn = new ColumnConstraints();
        listColumn.setPercentWidth(25);

        grid.getColumnConstraints().addAll(mainColumn, listColumn);
        grid.add(mainBox, 0, 0);
        grid.add(listView, 1, 0);

        GridPane.setHgrow(mainBox, Priority.ALWAYS);
        GridPane.setHgrow(listView, Priority.ALWAYS);
        GridPane.setVgrow(mainBox, Priority.ALWAYS);
        GridPane.setVgrow(listView, Priority.ALWAYS);

        Scene scene = new Scene(grid, 600, 400);
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