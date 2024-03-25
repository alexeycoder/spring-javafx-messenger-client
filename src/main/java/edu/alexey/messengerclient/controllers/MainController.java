package edu.alexey.messengerclient.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import edu.alexey.messengerclient.bundles.Lang;
import edu.alexey.messengerclient.services.ConnectionService;
import edu.alexey.messengerclient.utils.DialogManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

@Controller
public class MainController {

	private final BooleanProperty isInitializedProperty = new SimpleBooleanProperty(false);

	@Autowired
	private ConnectionService connectionService;

	@FXML
	private ComboBox<Lang> comboBoxLanguage;
	@FXML
	private TextField textFieldServerHost;
	@FXML
	private TextField textFieldServerPort;

	public ComboBox<Lang> getComboBoxLanguage() {
		return comboBoxLanguage;
	}

	public TextField getTextFieldServerHost() {
		return textFieldServerHost;
	}

	public TextField getTextFieldServerPort() {
		return textFieldServerPort;
	}

	@FXML
	private void initialize() {
		isInitializedProperty.set(false);

		isInitializedProperty.set(true);
	}

	public BooleanProperty isInitializedProperty() {
		return isInitializedProperty;
	}

	@FXML
	public void actionCheckConnection(ActionEvent event) {
		if (connectionService.checkConnection()) {
			DialogManager.showConfirmDialog("Успешно", "Сервер доступен.");
		} else {
			DialogManager.showConfirmDialog("Нет ответа", "Сервер не доступен.");
		}
	}

}
