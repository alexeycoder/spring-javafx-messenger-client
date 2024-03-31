package edu.alexey.messengerclient.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.alexey.messengerclient.bundles.Lang;
import edu.alexey.messengerclient.bundles.LocaleManager;
import edu.alexey.messengerclient.utils.CustomProperties;
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
			//subscribeCustomPropertiesChanged();
		} else {
			mainController.isInitializedProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue) {
					doInit(true, true);
					//subscribeCustomPropertiesChanged();
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
		//		Label labelDisplayName = mainController.getLabelDisplayName();
		//		Label labelUsername = mainController.getLabelUsername();
		//		Label labelPassword = mainController.getLabelPassword();
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
			textFieldDisplayName.setText(customProperties.getDisplayName().isBlank()
					? NO_DATA_PLACEHOLDER
					: customProperties.getDisplayName());
			textFieldUsername.setText(customProperties.getUsername().isBlank()
					? NO_DATA_PLACEHOLDER
					: customProperties.getUsername());
			passwordField.setText(customProperties.getPassword().isBlank()
					? NO_DATA_PLACEHOLDER
					: customProperties.getPassword());
			buttonConnect.setDisable(
					customProperties.getUsername().isBlank()
							|| customProperties.getPassword().isBlank());

			suppressUiChangeHandlers = false;
			customProperties.setSuppressPropertyChangeEvent(false);
		}

		if (subscribeUiChanges) {
			// Language
			comboBoxLanguage.setOnAction(event -> {
				if (!CustomPropertiesBinder.this.suppressUiChangeHandlers) {
					Lang selectedLang = comboBoxLanguage.getSelectionModel().getSelectedItem();
					LocaleManager.setCurrent(selectedLang);
					customProperties.setLanguage(selectedLang.getCode());
				}
			});

			// Server
			textFieldServerHost.textProperty().addListener((observable, oldValue, newValue) -> {
				if (!CustomPropertiesBinder.this.suppressUiChangeHandlers)
					customProperties.setServerHost(newValue);
			});
			textFieldServerPort.textProperty().addListener((observable, oldValue, newValue) -> {
				if (!CustomPropertiesBinder.this.suppressUiChangeHandlers)
					customProperties.setServerPort(newValue);
			});

			// Credentials
			textFieldDisplayName.textProperty().addListener((observable, oldValue, newValue) -> {
				if (!CustomPropertiesBinder.this.suppressUiChangeHandlers)
					customProperties.setDisplayName(newValue);
			});

			textFieldUsername.textProperty().addListener((observable, oldValue, newValue) -> {
				if (!CustomPropertiesBinder.this.suppressUiChangeHandlers)
					customProperties.setUsername(newValue);
			});
			passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
				if (!CustomPropertiesBinder.this.suppressUiChangeHandlers)
					customProperties.setPassword(newValue);
			});
		}
	}

	private void populateComboBoxLanguage(ComboBox<Lang> comboBoxLanguage) {
		if (comboBoxLanguage.getItems().isEmpty()) {
			//			comboBoxLanguage.setItems(FXCollections.<Lang>observableList(LocaleManager.LANGUAGES));
			comboBoxLanguage.getItems().addAll(LocaleManager.LANGUAGES);
			comboBoxLanguage.getSelectionModel().select(LocaleManager.getCurrent());
		}
	}

	private void subscribeCustomPropertiesChanged() {

		this.customProperties.addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equalsIgnoreCase("username")
					|| evt.getPropertyName().equals("displayName")
					|| evt.getPropertyName().equals("password")) {
				Platform.runLater(() -> {
					customProperties.setSuppressPropertyChangeEvent(true);
					suppressUiChangeHandlers = true;
					doInit(true, false);
					customProperties.setSuppressPropertyChangeEvent(false);
					suppressUiChangeHandlers = false;
				});
			}
		});
	}

}
