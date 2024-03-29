package edu.alexey.messengerclient.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import edu.alexey.messengerclient.bundles.Lang;
import edu.alexey.messengerclient.bundles.Messages;
import edu.alexey.messengerclient.dto.SignupDto;
import edu.alexey.messengerclient.services.ConnectionService;
import edu.alexey.messengerclient.utils.CustomProperties;
import edu.alexey.messengerclient.utils.DialogManager;
import edu.alexey.messengerclient.view.SignupView;
import edu.alexey.messengerclient.viewmodel.SignupViewModel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Getter;

@Controller
public class MainController {

	private final BooleanProperty isInitializedProperty = new SimpleBooleanProperty(false);

	@Autowired
	private ConnectionService connectionService;

	@Autowired
	private CustomProperties customProperties;

	@Autowired
	private SignupView signupView;

	@Getter
	@FXML
	private ComboBox<Lang> comboBoxLanguage;
	@Getter
	@FXML
	private TextField textFieldServerHost;
	@Getter
	@FXML
	private TextField textFieldServerPort;

	@Getter
	@FXML
	private Label labelDisplayName;
	@Getter
	@FXML
	private Label labelUsername;
	@Getter
	@FXML
	private Label labelPassword;
	@Getter
	@FXML
	private Button buttonConnect;
	@Getter
	@FXML
	private Label labelConnectionStatus;
	@Getter
	@FXML
	private Circle indicatorConnectionStatus;

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
			DialogManager.showConfirmDialog(Messages.getString("ui.main.settings.success"),
					Messages.getString("ui.main.settings.connection_available"));
		} else {
			DialogManager.showConfirmDialog(Messages.getString("ui.main.settings.failure"),
					Messages.getString("ui.main.settings.connection_unavailable"));
		}
	}

	@FXML
	public void actionSignup(ActionEvent event) {

		var result = showSignupDialog(((Node) event.getSource()).getScene().getWindow(), new SignupDto());
		result.ifPresent(signupData -> {
			customProperties.setDisplayName(signupData.getDisplayName());
			customProperties.setUsername(signupData.getUsername());
			customProperties.setPassword(signupData.getPassword());
		});
	}

	private Optional<SignupDto> showSignupDialog(Window owner, SignupDto signupData) {
		if (signupData == null) {
			throw new NullPointerException();
		}

		SignupViewModel viewModel = new SignupViewModel(signupData);

		Stage stage = new Stage();
		Parent parent = signupView.getRootNode();
		signupView.setViewModel(viewModel);

		Scene scene = new Scene(parent, 450, 250);

		stage.setScene(scene);
		stage.sizeToScene();
		stage.setMinWidth(400);
		stage.setMinHeight(250);
		stage.setTitle(Messages.getString("ui.dialog.signup.title")); //$NON-NLS-1$
		stage.initModality(Modality.WINDOW_MODAL);
		if (owner != null) {
			stage.initOwner(owner);
		}
		stage.setOnShown(event -> {
			Platform.runLater(() -> {
				stage.setWidth(450);
				stage.setHeight(250);
			});
		});
		stage.showAndWait();

		stage.setScene(null);
		scene.setRoot(new Group()); // удаляем имеющийся корневой узел из графа

		return Optional.ofNullable(viewModel.getResult());
	}

	@FXML
	public void actionConnect(ActionEvent event) {

		if (!connectionService.checkAuthorization()) {
			DialogManager.showConfirmDialog(Messages.getString("ui.main.settings.failure"),
					Messages.getString("ui.main.settings.login_unauthorized"));
			return;
		}

		connectionService.login();

		indicatorConnectionStatus.setFill(Color.GREEN);
		labelConnectionStatus.setText(Messages.getString("ui.main.status.connected"));
	}

}
