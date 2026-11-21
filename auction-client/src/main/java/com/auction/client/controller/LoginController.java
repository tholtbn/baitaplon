package com.auction.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    private void handleLogin() {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText();
        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("Vui lòng nhập đầy đủ tài khoản và mật khẩu.");
            return;
        }
        lblError.setText("Đang đăng nhập với tài khoản: " + user + "...");
    }

    @FXML
    private void handleGoToRegister() {
        lblError.setText("Chức năng Đăng ký sẽ được nối ở bước tiếp theo.");
    }
}