package edu.alexey.messengerclient.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.alexey.messengerclient.bundles.Lang;
import edu.alexey.messengerclient.bundles.LocaleManager;
import edu.alexey.messengerclient.utils.CustomProperties;
import jakarta.annotation.PostConstruct;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

@Component
public class CustomPropertiesBinder {

	@Autowired
	private CustomProperties customProperties;

	@Autowired
	private MainController mainController;

	@PostConstruct
	void init() {

		if (mainController.isInitializedProperty().get()) {
			doInit();
		} else {
			mainController.isInitializedProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue) {
					doInit();
				}
			});
		}
	}

	private void doInit() {

		actualizeElements();

		ComboBox<Lang> comboBoxLanguage = mainController.getComboBoxLanguage();
		comboBoxLanguage.setOnAction(event -> {
			Lang selectedLang = comboBoxLanguage.getSelectionModel().getSelectedItem();
			LocaleManager.setCurrent(selectedLang);
			customProperties.setLanguage(selectedLang.getCode());
		});

		TextField textFieldServerHost = mainController.getTextFieldServerHost();
		textFieldServerHost.textProperty().addListener((observable, oldValue, newValue) -> {
			customProperties.setServerHost(newValue);
		});

		TextField textFieldServerPort = mainController.getTextFieldServerPort();
		textFieldServerPort.textProperty().addListener((observable, oldValue, newValue) -> {
			customProperties.setServerPort(newValue);
		});
	}

	private void actualizeElements() {
		populateComboBoxLanguage(mainController.getComboBoxLanguage());
		mainController.getTextFieldServerHost().setText(customProperties.getServerHost());
		mainController.getTextFieldServerPort().setText(customProperties.getServerPort());
	}

	private void populateComboBoxLanguage(ComboBox<Lang> comboBoxLanguage) {
		comboBoxLanguage.setItems(FXCollections.<Lang>observableList(LocaleManager.LANGUAGES));
		comboBoxLanguage.getSelectionModel().select(LocaleManager.getCurrent());
	}

}
