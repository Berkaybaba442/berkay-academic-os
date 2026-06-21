package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

public class WebModuleView {

    public SplitPane build(
            ModuleItem module,
            WebView webView,
            TextArea notesArea,
            Runnable saveAction,
            Runnable clearAction
    ) {
        if (module.getTarget() != null && !module.getTarget().isBlank()) {
            webView.getEngine().load(module.getTarget());
        }

        Label notesTitle = new Label(module.getName() + " Notları");
        notesTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button saveButton = new Button("Kaydet");
        Button clearButton = new Button("Temizle");

        saveButton.setOnAction(e -> saveAction.run());
        clearButton.setOnAction(e -> clearAction.run());

        HBox notesToolbar = new HBox(10, saveButton, clearButton);

        VBox notesPanel = new VBox(10, notesTitle, notesToolbar, notesArea);
        notesPanel.setPadding(new Insets(10));
        notesPanel.setPrefWidth(320);
        notesPanel.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(notesArea, Priority.ALWAYS);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(webView, notesPanel);
        splitPane.setDividerPositions(0.72);

        return splitPane;
    }
}