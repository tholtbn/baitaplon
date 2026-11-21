package com.auction.client.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public final class SceneNavigator {
    private static Stage primaryStage;

    private SceneNavigator() {}

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static void navigate(String fxmlPath, String title) {
        navigate(fxmlPath, title, null);
    }

    public static <T> void navigate(String fxmlPath, String title, Consumer<T> controllerInit) {
        try {
            URL resource = SceneNavigator.class.getResource(fxmlPath);
            if (resource == null) {
                throw new IllegalArgumentException("Không tìm thấy FXML: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            if (controllerInit != null) {
                controllerInit.accept(loader.getController());
            }

            Scene scene = new Scene(root);
            URL css = SceneNavigator.class.getResource("/css/style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi nạp màn hình: " + fxmlPath, e);
        }
    }
}