package edu.alexey.messengerclient.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.alexey.messengerclient.bundles.Lang;
import edu.alexey.messengerclient.bundles.LocaleManager;
import edu.alexey.messengerclient.utils.CustomProperties;
import edu.alexey.messengerclient.utils.StringUtils;
import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

		Button buttonConnect = mainController.getButtonConnect();

		if (updateUi) {

			suppressUiChangeHandlers = true;
			customProperties.setSuppressPropertyChangeEvent(true);

			// Language
			populateComboBoxLanguage(comboBoxLanguage);

			// Server
			textFieldServerHost.setText(customProperties.getServerHost());
			textFieldServerPort.setText(customProperties.getServerPort());

			// Credentials

			textFieldDisplayName.setText(customProperties.getDisplayName());
			textFieldUsername.setText(customProperties.getUsername());
			passwordField.setText(customProperties.getPassword());
			buttonConnect.setDisable(
					customProperties.getUsername() == null
							|| customProperties.getPassword() == null);

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
					evt.getNewValue() == null ? null : evt.getNewValue().toString());

		});
	}

	private void handleCredentialsPropertiesChanged(String propertyName, String newValue) {
		TextField textField = switch (propertyName) {
		case "username" -> mainController.getTextFieldUsername();
		case "displayName" -> mainController.getTextFieldDisplayName();
		case "password" -> mainController.getPasswordField();
		default -> null;
		};

		if (textField == null) {
			return;
		}

		Runnable action = () -> {
			customProperties.setSuppressPropertyChangeEvent(true);
			suppressUiChangeHandlers = true;
			int pos = textField.getCaretPosition();
			textField.setText(newValue);
			textField.positionCaret(pos);
			customProperties.setSuppressPropertyChangeEvent(false);
			suppressUiChangeHandlers = false;

			mainController.getButtonConnect().setDisable(
					StringUtils.isNullOrBlank(customProperties.getUsername())
							|| StringUtils.isNullOrBlank(customProperties.getPassword()));
		};

		if (Platform.isFxApplicationThread()) {
			// Если делать в "отложенной" манере, то при быстром наборе текста в поле
			// корректировка позиции курсора может иногда не успевать за вводом.
			action.run();
		} else {
			Platform.runLater(action);
		}

	}

}
