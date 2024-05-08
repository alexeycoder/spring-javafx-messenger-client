package edu.alexey.messengerclient.controllers;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import edu.alexey.messengerclient.bundles.Messages;
import edu.alexey.messengerclient.dto.ContactDto;
import edu.alexey.messengerclient.services.MessagingService;
import edu.alexey.messengerclient.utils.StringUtils;
import edu.alexey.messengerclient.utils.TableViewUtils;
import edu.alexey.messengerclient.viewmodel.FindContactViewModelConsumer;
import edu.alexey.messengerclient.viewmodel.ReadOnlyStringValueFactory;
import edu.alexey.messengerclient.viewmodel.abstractions.ViewModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
	private TableView<ContactDto> tableViewSearchResult;
	@FXML
	private TableColumn<ContactDto, String> tableColumnDisplayName;
	@FXML
	private TableColumn<ContactDto, String> tableColumnUserUuid;
	@FXML
	private Label labelNothingFound;
	@FXML
	private Button buttonAddToConversations;

	private ViewModel<Void, List<ContactDto>> contactViewModel;

	@FXML
	private void initialize() {
		System.out.println(
				"FIND initialize THREAD: " + Thread.currentThread().getName() + " ID = "
						+ Thread.currentThread().threadId());

		labelNothingFound.setVisible(false);
		tableViewSearchResult.setVisible(false);

		// tableColumnDisplayName.setCellValueFactory(new PropertyValueFactory<ContactDto, String>("displayName"));
		// tableColumnUserUuid.setCellValueFactory(new PropertyValueFactory<ContactDto, String>("userUuid"));
		tableColumnDisplayName.setCellValueFactory(
				new ReadOnlyStringValueFactory<ContactDto>("displayName", dto -> dto.displayName()));
		tableColumnUserUuid.setCellValueFactory(
				new ReadOnlyStringValueFactory<ContactDto>("userUuid", dto -> dto.userUuid().toString()));

		if (comboBoxFindBy.getItems().isEmpty()) {
			comboBoxFindBy.getItems().setAll(FindByMetric.values());
			comboBoxFindBy.getSelectionModel().select(0);
		}

		buttonFind.setDisable(StringUtils.isNullOrBlank(textFieldTextToFind.getText()));
		buttonAddToConversations.setDisable(tableViewSearchResult.getSelectionModel().isEmpty());

		textFieldTextToFind.textProperty()
				.addListener(
						(observable, oldValue, newValue) -> buttonFind.setDisable(StringUtils.isNullOrBlank(newValue)));
		tableViewSearchResult.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tableViewSearchResult.getSelectionModel().selectedItemProperty()
				.addListener(observable -> buttonAddToConversations
						.setDisable(tableViewSearchResult.getSelectionModel().isEmpty()));
	}

	@Override
	public void accept(ViewModel<Void, List<ContactDto>> viewModel) {
		contactViewModel = Objects.requireNonNull(viewModel);
		contactViewModel.setResult(List.of());
	}

	@FXML
	public void actionClose(ActionEvent event) {

		System.out.println(
				"FIND CLOSE THREAD: " + Thread.currentThread().getName() + " ID = "
						+ Thread.currentThread().threadId());
		//		clearElements();
		if (event.getSource() instanceof Node node) {
			node.getScene().getWindow().fireEvent(new Event(WindowEvent.WINDOW_CLOSE_REQUEST));
		}
	}

	@FXML
	public void actionFind(ActionEvent event) {

		tableViewSearchResult.getItems().clear();
		labelNothingFound.setVisible(false);
		tableViewSearchResult.setVisible(false);

		String patternToFind = textFieldTextToFind.getText();

		if (StringUtils.isNullOrBlank(patternToFind)) {
			labelNothingFound.setVisible(false);
			tableViewSearchResult.setVisible(false);
			return;
		}

		FindByMetric selectedMetric = comboBoxFindBy.getSelectionModel().getSelectedItem();

		List<ContactDto> found = switch (selectedMetric) {
		case DISPLAY_NAME -> messagingService.findUsersByDisplayName(patternToFind);
		case USER_IDENTIFIER -> messagingService.findUsersByUserUuid(patternToFind);
		default -> throw new NoSuchElementException();
		};

		if (found.isEmpty()) {
			tableViewSearchResult.setVisible(false);
			labelNothingFound.setVisible(true);
			return;
		}

		tableViewSearchResult.setVisible(true);
		labelNothingFound.setVisible(false);

		tableViewSearchResult.getItems().addAll(found);

		Platform.runLater(() -> {
			TableViewUtils.resizeColumn(tableViewSearchResult, "tableColumnDisplayName");
		});
	}

	@FXML
	public void actionAddContact(ActionEvent event) {

		var selection = tableViewSearchResult.getSelectionModel().getSelectedItems();
		if (!selection.isEmpty()) {
			contactViewModel.setResult(List.copyOf(selection));
		}
		actionClose(event);
	}

}
