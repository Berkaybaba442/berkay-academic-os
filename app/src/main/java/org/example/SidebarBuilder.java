package org.example;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class SidebarBuilder {

    public VBox build(AppConfig appConfig, Consumer<ModuleItem> onModuleClick) {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(15));
        sidebar.setStyle("-fx-background-color: #f4f4f4;");

        double width = 220;
        if (appConfig != null && appConfig.getUi() != null) {
            width = appConfig.getUi().getSidebarWidth();
        }
        sidebar.setPrefWidth(width);

        if (appConfig == null || appConfig.getModules() == null) {
            return sidebar;
        }

        for (ModuleItem module : appConfig.getModules()) {
            if (!module.isEnabled()) {
                continue;
            }

            Button button = new Button(module.getName());
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(e -> onModuleClick.accept(module));
            sidebar.getChildren().add(button);
        }

        return sidebar;
    }
}