package edu.alexey.messengerclient.utils;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class DialogManager {

	public static void showInfoDialog(String title, String header, String text) {
		showDialog(AlertType.INFORMATION, title, header, text);
	}

	public static void showErrorDialog(String title, String text) {
		showDialog(AlertType.ERROR, title, null, text);
	}

	public static Optional<ButtonType> showConfirmDialog(String title, String text) {
		return showDialog(AlertType.CONFIRMATION, title, null, text);
	}

	private static Optional<ButtonType> showDialog(AlertType alertType, String title, String header, String text) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(text);
		Optional<ButtonType> result = alert.showAndWait();
		return result;
	}

}
