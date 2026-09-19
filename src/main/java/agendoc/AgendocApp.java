package agendoc;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Main entry point for the Agendoc JavaFX application.
 */
public class AgendocApp extends Application
{

    private ListView<DocumentRef> listView;
    private Button deleteButton;
    private MenuItem deleteMenuItem;

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

        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> addDocument(primaryStage));

        deleteButton = new Button("Удалить");
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> deleteSelectedDocument());

        HBox buttonBox = new HBox(10, llmButton, addButton, deleteButton);

        TextArea textArea = new TextArea();
        textArea.setPromptText("Enter multi-line text here...");
        textArea.setPrefRowCount(10);

        TextField textField = new TextField();
        textField.setPromptText("Enter text here...");

        listView = new ListView<>();
        listView.setCellFactory(lv -> new ListCell<>()
        {
            @Override
            protected void updateItem(DocumentRef item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty || item == null)
                {
                    setText(null);
                }
                else
                {
                    setText(item.toString());
                }
            }
        });

        listView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) ->
            {
                boolean hasSelection = newVal != null;
                deleteButton.setDisable(!hasSelection);
                deleteMenuItem.setDisable(!hasSelection);
            });

        MenuItem addMenuItem = new MenuItem("Добавить");
        addMenuItem.setOnAction(e -> addDocument(primaryStage));

        deleteMenuItem = new MenuItem("Удалить");
        deleteMenuItem.setDisable(true);
        deleteMenuItem.setOnAction(e -> deleteSelectedDocument());

        ContextMenu contextMenu = new ContextMenu(addMenuItem, deleteMenuItem);
        listView.setContextMenu(contextMenu);

        VBox mainBox = new VBox(10, buttonBox, textArea, textField);
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
     * Opens a file chooser dialog and adds the selected file to the document list.
     *
     * @param owner the parent stage for the file chooser dialog
     */
    private void addDocument(Stage owner)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите документ");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Документы Microsoft Office",
                "*.docx", "*.xlsx", "*.doc", "*.xls"),
            new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );
        File selectedFile = fileChooser.showOpenDialog(owner);
        if (selectedFile != null)
        {
            DocumentRef docRef = new DocumentRef(selectedFile.toPath().toAbsolutePath());
            listView.getItems().add(docRef);
        }
    }

    /**
     * Removes the currently selected document from the list.
     */
    private void deleteSelectedDocument()
    {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0)
        {
            listView.getItems().remove(selectedIndex);
        }
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