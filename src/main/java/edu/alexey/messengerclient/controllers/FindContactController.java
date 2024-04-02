package edu.alexey.messengerclient.controllers;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import edu.alexey.messengerclient.bundles.Messages;
import edu.alexey.messengerclient.dto.ContactDto;
import edu.alexey.messengerclient.services.MessagingService;
import edu.alexey.messengerclient.utils.StringUtils;
import edu.alexey.messengerclient.viewmodel.FindContactViewModelConsumer;
import edu.alexey.messengerclient.viewmodel.abstractions.ViewModel;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;

@Controller
public class FindContactController implements FindContactViewModelConsumer {

	static enum FindByMetric {
		USER_IDENTIFIER("ui.dialog.find_contact.by_user_id"),
		DISPLAY_NAME("ui.dialog.find_contact.by_display_name");

		final String label;

		private FindByMetric(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return Messages.getString(label);
		}
	}

	@Autowired
	private MessagingService messagingService;

	@FXML
	private ComboBox<FindByMetric> comboBoxFindBy;
	@FXML
	private TextField textFieldTextToFind;
	@FXML
	private Button buttonFind;
	@FXML
	private ListView<ContactDto> listViewSearchResult;
	@FXML
	private Label labelNothingFound;
	@FXML
	private Button buttonAddToConversations;

	private ViewModel<Void, ContactDto> contactViewModel;

	@FXML
	private void initialize() {

		if (comboBoxFindBy.getItems().isEmpty()) {
			comboBoxFindBy.getItems().setAll(FindByMetric.values());
			comboBoxFindBy.getSelectionModel().select(0);
		}

		buttonFind.setDisable(StringUtils.isNullOrBlank(textFieldTextToFind.getText()));
		buttonAddToConversations.setDisable(listViewSearchResult.getSelectionModel().isEmpty());

		textFieldTextToFind.textProperty()
				.addListener(
						(observable, oldValue, newValue) -> buttonFind.setDisable(StringUtils.isNullOrBlank(newValue)));
		listViewSearchResult.getSelectionModel().selectedItemProperty()
				.addListener(observable -> buttonAddToConversations
						.setDisable(listViewSearchResult.getSelectionModel().isEmpty()));
	}

	@Override
	public void accept(ViewModel<Void, ContactDto> viewModel) {
		this.contactViewModel = Objects.requireNonNull(viewModel);
		this.contactViewModel.setResult(null);
	}

	@FXML
	public void actionClose(ActionEvent event) {
		//		clearElements();
		if (event.getSource() instanceof Node node) {
			node.getScene().getWindow().fireEvent(new Event(WindowEvent.WINDOW_CLOSE_REQUEST));
		}
	}

	@FXML
	public void actionFind(ActionEvent event) {

	}

}
