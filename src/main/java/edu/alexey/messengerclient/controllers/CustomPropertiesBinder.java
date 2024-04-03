package edu.alexey.messengerclient.controllers;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.alexey.messengerclient.bundles.Lang;
import edu.alexey.messengerclient.bundles.LocaleManager;
import edu.alexey.messengerclient.utils.CustomProperties;
import edu.alexey.messengerclient.utils.StringUtils;
import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

@Component
public class CustomPropertiesBinder {

	static final String NO_DATA_PLACEHOLDER = "\u2014";

	@Autowired
	private CustomProperties customProperties;

	@Autowired
	private MainController mainController;

	private volatile boolean suppressUiChangeHandlers;

	@PostConstruct
	void init() {

		if (mainController.isInitializedProperty().get()) {
			doInit(true, true);
			subscribeCustomPropertiesChanged();
		} else {
			mainController.isInitializedProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue) {
					doInit(true, true);
					subscribeCustomPropertiesChanged();
				}
			});
		}
	}

	private void doInit(boolean updateUi, boolean subscribeUiChanges) {

		// Language
		ComboBox<Lang> comboBoxLanguage = mainController.getComboBoxLanguage();
		// Server
		TextField textFieldServerHost = mainController.getTextFieldServerHost();
		TextField textFieldServerPort = mainController.getTextFieldServerPort();
		// Credentials
		TextField textFieldDisplayName = mainController.getTextFieldDisplayName();
		TextField textFieldUsername = mainController.getTextFieldUsername();
		PasswordField passwordField = mainController.getPasswordField();

		if (updateUi) {

			suppressUiChangeHandlers = true;
			customProperties.setSuppressPropertyChangeEvent(true);

			// Language
			populateComboBoxLanguage(comboBoxLanguage);

			// Server
			textFieldServerHost.setText(customProperties.getServerHost());
			textFieldServerPort.setText(customProperties.getServerPort());

			// Credentials

			//			labelUserUuid.setText(userUuidStr);
			//			textFieldUserUuid.setText(userUuidStr);
			//			textFieldDisplayName.setText(customProperties.getDisplayName());
			//			textFieldUsername.setText(customProperties.getUsername());
			//			passwordField.setText(customProperties.getPassword());
			//			buttonConnect.setDisable(
			//					customProperties.getUsername() == null
			//							|| customProperties.getPassword() == null);

			actualizeUserUuidIndications(customProperties.getUserUuid());
			actualizeTextField(textFieldDisplayName, customProperties.getDisplayName());
			actualizeTextField(textFieldUsername, customProperties.getUsername());
			actualizeTextField(passwordField, customProperties.getPassword());
			actualizeButtonConnect();

			suppressUiChangeHandlers = false;
			customProperties.setSuppressPropertyChangeEvent(false);
		}

		if (subscribeUiChanges) {
			// Language
			comboBoxLanguage.setOnAction(event -> {
				if (!suppressUiChangeHandlers) {
					Lang selectedLang = comboBoxLanguage.getSelectionModel().getSelectedItem();
					LocaleManager.setCurrent(selectedLang);
					customProperties.setLanguage(selectedLang.getCode());
				}
			});

			// Server
			textFieldServerHost.textProperty().addListener((observable, oldValue, newValue) -> {
				if (!suppressUiChangeHandlers)
					customProperties.setServerHost(newValue);
			});
			textFieldServerPort.textProperty().addListener((observable, oldValue, newValue) -> {
				if (!suppressUiChangeHandlers)
					customProperties.setServerPort(newValue);
			});

			// Credentials
			textFieldDisplayName.textProperty().addListener((observable, oldValue, newValue) -> {
				if (!suppressUiChangeHandlers)
					customProperties.setDisplayName(newValue);
			});

			textFieldUsername.textProperty().addListener((observable, oldValue, newValue) -> {
				if (!suppressUiChangeHandlers)
					customProperties.setUsername(newValue);
			});
			passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
				if (!suppressUiChangeHandlers)
					customProperties.setPassword(newValue);
			});
		}
	}

	private void populateComboBoxLanguage(ComboBox<Lang> comboBoxLanguage) {
		if (comboBoxLanguage.getItems().isEmpty()) {
			comboBoxLanguage.getItems().addAll(LocaleManager.LANGUAGES);
			comboBoxLanguage.getSelectionModel().select(LocaleManager.getCurrent());
		}
	}

	private void subscribeCustomPropertiesChanged() {

		this.customProperties.addPropertyChangeListener(evt -> {

			handleCredentialsPropertiesChanged(
					evt.getPropertyName(),
					evt.getNewValue());

			handleUserUuidPropertyChanged(evt.getPropertyName(), evt.getNewValue());

		});
	}

	private void handleCredentialsPropertiesChanged(String propertyName, Object newValue) {

		TextField textField = switch (propertyName) {
		case "username" -> mainController.getTextFieldUsername();
		case "displayName" -> mainController.getTextFieldDisplayName();
		case "password" -> mainController.getPasswordField();
		default -> null;
		};

		if (textField == null) {
			return;
		}

		actualizeTextField(textField, newValue);
		actualizeButtonConnect();
	}

	private void actualizeTextField(TextField textField, Object newValue) {
		assert textField != null : "textField must not be null";

		String newValueStr = Objects.toString(newValue, null);

		Runnable action = () -> {
			customProperties.setSuppressPropertyChangeEvent(true);
			suppressUiChangeHandlers = true;
			int pos = textField.getCaretPosition();
			textField.setText(newValueStr);
			textField.positionCaret(pos);
			customProperties.setSuppressPropertyChangeEvent(false);
			suppressUiChangeHandlers = false;
		};

		if (Platform.isFxApplicationThread()) {
			// Если делать в "отложенной" манере, то при быстром наборе текста в поле
			// корректировка позиции курсора может иногда не успевать за вводом.
			action.run();
		} else {
			Platform.runLater(action);
		}
	}

	private void actualizeButtonConnect() {
		mainController.getButtonConnect().setDisable(
				StringUtils.isNullOrBlank(customProperties.getUsername())
						|| StringUtils.isNullOrBlank(customProperties.getPassword()));
	}

	private void handleUserUuidPropertyChanged(String propertyName, Object newValue) {

		if (!"userUuid".equals(propertyName)) {
			return;
		}

		actualizeUserUuidIndications(newValue);
	}

	private void actualizeUserUuidIndications(Object newValue) {
		String strLabel = newValue != null ? newValue.toString() : NO_DATA_PLACEHOLDER;
		String strTextField = newValue != null ? newValue.toString() : null;
		Label labelUserUuid = mainController.getLabelUserUuid();
		TextField textFieldUserUuid = mainController.getTextFieldUserUuid();
		Platform.runLater(() -> {
			labelUserUuid.setText(strLabel);
			textFieldUserUuid.setText(strTextField);
		});
	}

}
