package edu.alexey.messengerclient.controllers;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import edu.alexey.messengerclient.bundles.Messages;
import edu.alexey.messengerclient.dto.SignupDto;
import edu.alexey.messengerclient.services.ConnectionService;
import edu.alexey.messengerclient.utils.DialogManager;
import edu.alexey.messengerclient.viewmodel.SignupViewModelConsumer;
import edu.alexey.messengerclient.viewmodel.abstractions.ViewModel;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;

@Controller
public class SignupController implements SignupViewModelConsumer {

	@Autowired
	private ConnectionService connectionService;

	@FXML
	private TextField textFieldDisplayName;
	@FXML
	private TextField textFieldUsername;
	@FXML
	private PasswordField passwordField;

	private ViewModel<SignupDto, SignupDto> signupViewModel;

	@Override
	public void accept(ViewModel<SignupDto, SignupDto> viewModel) {
		signupViewModel = Objects.requireNonNull(viewModel);
		signupViewModel.setResult(null);
		actualizeView(viewModel.getInput());
	}

	private void actualizeView(SignupDto signupData) {
		if (signupData == null) {
			signupData = new SignupDto();
			clearElements();
			return;
		}

		textFieldDisplayName.setText(signupData.getDisplayName());
		textFieldUsername.setText(signupData.getUsername());
		passwordField.setText(signupData.getPassword());
	}

	private void clearElements() {

		textFieldDisplayName.clear();
		textFieldUsername.clear();
		passwordField.clear();
	}

	@FXML
	public void actionSignup(ActionEvent event) {

		if (!checkValidity()) {
			DialogManager.showErrorDialog(
					Messages.getString("ui.error"),
					Messages.getString("ui.main.settings.all_fields_required"));
			return;
		}

		SignupDto signupData = new SignupDto();
		signupData.setDisplayName(textFieldDisplayName.getText());
		signupData.setUsername(textFieldUsername.getText());
		signupData.setPassword(passwordField.getText());

		if (!connectionService.trySignup(signupData)) {
			DialogManager.showErrorDialog(
					Messages.getString("ui.error"),
					Messages.getString("ui.main.settings.registration_failed"));

			return;
		}

		signupViewModel.setResult(signupData);

		DialogManager.showInfoDialog(
				Messages.getString("ui.main.settings.success"),
				Messages.getString("ui.main.settings.registration_success"),
				Messages.getString("ui.main.settings.registration_after"));

		actionClose(event);
	}

	@FXML
	public void actionClose(ActionEvent event) {
		clearElements();
		if (event.getSource() instanceof Node node) {
			node.getScene().getWindow().fireEvent(new Event(WindowEvent.WINDOW_CLOSE_REQUEST));
			//((Stage) node.getScene().getWindow()).close();
		}
	}

	private boolean checkValidity() {
		return !(textFieldDisplayName.getText().isBlank()
				|| textFieldUsername.getText().isBlank()
				|| passwordField.getText().isBlank());
	}

}
