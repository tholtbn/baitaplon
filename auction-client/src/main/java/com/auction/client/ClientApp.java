package com.auction.client;

import com.auction.client.view.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class ClientApp extends Application {
    @Override
    public void start(Stage stage) {
        SceneNavigator.init(stage);
        stage.setMinWidth(480);
        stage.setMinHeight(360);
        SceneNavigator.navigate("/fxml/login.fxml", "Online Auction - Đăng nhập");
    }

    public static void main(String[] args) {
        launch(args);
    }
}