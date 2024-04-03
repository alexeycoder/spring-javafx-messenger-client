package edu.alexey.messengerclient.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import edu.alexey.messengerclient.bundles.Lang;
import edu.alexey.messengerclient.bundles.Messages;
import edu.alexey.messengerclient.dto.ContactDto;
import edu.alexey.messengerclient.dto.SignupDto;
import edu.alexey.messengerclient.entities.Conversation;
import edu.alexey.messengerclient.entities.Message;
import edu.alexey.messengerclient.services.ConnectionService;
import edu.alexey.messengerclient.services.ConversationService;
import edu.alexey.messengerclient.services.MessageService;
import edu.alexey.messengerclient.services.MessagingService;
import edu.alexey.messengerclient.utils.CustomProperties;
import edu.alexey.messengerclient.utils.DialogManager;
import edu.alexey.messengerclient.view.ConversationListViewCell;
import edu.alexey.messengerclient.view.FindContactView;
import edu.alexey.messengerclient.view.MessageListViewCell;
import edu.alexey.messengerclient.view.SignupView;
import edu.alexey.messengerclient.viewmodel.BasicViewModel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
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
	private CustomProperties customProperties;
	@Autowired
	private ConnectionService connectionService;
	@Autowired
	private MessagingService messagingService;
	@Autowired
	private ConversationService conversationService;
	@Autowired
	private MessageService messageService;

	//	@Autowired
	//	private MessageRepository messageRepository;
	//	@Autowired
	//	private ConversationRepository conversationRepository;
	//	@Autowired
	//	private ContactRepository contactRepository;
	//	@Autowired
	//	private TransactionTemplate transactionTemplate;

	@Autowired
	private SignupView signupView;

	@Autowired
	private FindContactView findContactView;

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
	private TextField textFieldDisplayName;
	@Getter
	@FXML
	private TextField textFieldUsername;
	@Getter
	@FXML
	private PasswordField passwordField;
	@Getter
	@FXML
	private Button buttonConnect;

	@Getter
	@FXML
	private Label labelConnectionStatus;
	@Getter
	@FXML
	private Circle indicatorConnectionStatus;
	@Getter
	@FXML
	private Label labelUserUuid;
	@Getter
	@FXML
	private TextField textFieldUserUuid;

	@FXML
	private Accordion accordionSettings;
	@FXML
	private Tab tabCommunication;

	@FXML
	private TabPane tabPaneMain;

	@FXML
	private Label labelCurrentConversation;

	@FXML
	private TextArea textAreaMessageToSend;

	@FXML
	private ListView<Conversation> listViewConversations;

	@FXML
	private ListView<String> listViewContacts;

	@FXML
	private ListView<Message> listViewCurrentConversation;

	private final ObservableList<Conversation> conversations;
	private final ObservableList<Message> conversationMessages;

	public MainController() {
		this.conversations = FXCollections.observableArrayList();
		this.conversationMessages = FXCollections.observableArrayList();
	}

	@FXML
	private void initialize() {
		isInitializedProperty.set(false);
		//
		accordionSettings.setExpandedPane(accordionSettings.getPanes().getFirst());

		listViewCurrentConversation.setCellFactory(lv -> new MessageListViewCell());
		listViewCurrentConversation.setItems(conversationMessages);

		listViewConversations.setCellFactory(lv -> new ConversationListViewCell(customProperties.getUserUuid()));
		listViewConversations.setItems(conversations);

		listViewConversations.getSelectionModel().selectedItemProperty()
				.addListener(this::handleListViewConversationsSelectionChanged);

		listViewCurrentConversation.setOnScrollFinished(event -> {
			if (event.getSource() instanceof Node node) {
				Platform.runLater(() -> {
					node.autosize();
					listViewCurrentConversation.getChildrenUnmodifiable().forEach(Node::autosize);
					listViewCurrentConversation.autosize();
				});
			}
		});

		loadCache();

		messagingService.setOnIncomingMessagesCallback(this::handleIncomingMessages);

		//
		isInitializedProperty.set(true);
	}

	public BooleanProperty isInitializedProperty() {
		return isInitializedProperty;
	}

	private void loadCache() {
		List<Conversation> conversations = conversationService.findAll();
		this.conversations.clear();
		this.conversations.addAll(conversations);
	}

	private void handleIncomingMessages(List<Conversation> newConversations, List<Message> newMessages) {
		Conversation currentConversation = listViewConversations.getSelectionModel().getSelectedItem();
		if (currentConversation != null) {
			List<Message> currentConversationMessages = newMessages.stream()
					.filter(m -> m.getConversation().getConversationId()
							.equals(currentConversation.getConversationId()))
					.toList();
			if (!currentConversationMessages.isEmpty()) {
				Platform.runLater(() -> {
					this.conversationMessages.addAll(currentConversationMessages);
					this.listViewCurrentConversation.scrollTo(this.listViewCurrentConversation.getItems().size() - 1);
				});
			}
		}

		if (!newConversations.isEmpty()) {
			Platform.runLater(() -> conversations.addAll(newConversations));
		}
	}

	private void handleListViewConversationsSelectionChanged(
			ObservableValue<? extends Conversation> observable,
			Conversation oldValue,
			Conversation newValue) {

		conversationMessages.clear();
		if (newValue == null) {
			Platform.runLater(() -> labelCurrentConversation
					.setText(Messages.getString("ui.main.communication.select_conversation")));
			return;
		}
		//		String displayName = transactionTemplate.execute(status -> newValue.getContact().getDisplayName());
		List<Message> messages = messageService.findAllByConversation(newValue);

		Platform.runLater(() -> {
			labelCurrentConversation.setText(newValue.getContact().getDisplayName());
			conversationMessages.addAll(messages);
			listViewCurrentConversation.autosize();
			listViewCurrentConversation.scrollTo(messages.size() - 1);
		});
	}

	//	@PostConstruct
	//	private void init() {
	//
	//		Faker faker = new Faker();
	//
	//		Contact contact1 = new Contact(UUID.randomUUID(), faker.name().username());
	//		Contact contact2 = new Contact(UUID.randomUUID(), faker.name().username());
	//		contactRepository.saveAllAndFlush(List.of(contact1, contact2));
	//
	//		Conversation conversation1 = new Conversation();
	//		conversation1.setContact(contact1);
	//		Conversation conversation2 = new Conversation();
	//		conversation2.setContact(contact2);
	//		conversationRepository.saveAllAndFlush(List.of(conversation1, conversation2));
	//
	//		ArrayList<Message> messages = new ArrayList<Message>();
	//		for (int i = 0; i < 20; ++i) {
	//
	//			Message message = new Message();
	//			message.setMessageUuid(UUID.randomUUID());
	//			message.setConversation(conversation1);
	//			message.setOwn(ThreadLocalRandom.current().nextBoolean());
	//			message.setSentAt(faker.date()
	//					.past(10, TimeUnit.DAYS)
	//					.toInstant().atZone(ZoneId.systemDefault())
	//					.toLocalDateTime());
	//			message.setContent(faker.lorem().sentence(ThreadLocalRandom.current().nextInt(5, 20)));
	//			messages.add(message);
	//			System.out.println("TEST MSG: " + message);
	//		}
	//		messageRepository.saveAllAndFlush(messages);
	//
	//		System.out.println("TOP = " + messageRepository.getTopBy(Sort.by("sentAt", "messageId").descending()));
	//	}

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

		var viewModel = new BasicViewModel<SignupDto, SignupDto>(signupData);

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

		if (!connectionService.login()) {
			DialogManager.showConfirmDialog(Messages.getString("ui.main.settings.failure"),
					Messages.getString("ui.main.settings.login_unauthorized"));
			return;
		}

		indicatorConnectionStatus.setFill(Color.GREEN);
		labelConnectionStatus.setText(Messages.getString("ui.main.status.connected"));

		//printConversationsForTest();

		tabPaneMain.getSelectionModel().selectNext();
	}

	@FXML
	public void actionSend(ActionEvent event) {
		String text = textAreaMessageToSend.getText();
		if (text.isEmpty()) {
			return;
		}

		Conversation currentConversation = listViewConversations.getSelectionModel().getSelectedItem();
		if (currentConversation == null) {
			return;
		}

		if (messagingService.sendMessage(currentConversation.getConversationId(), text)) {
			textAreaMessageToSend.clear();
		} else {
			DialogManager.showErrorDialog(
					Messages.getString("ui.error"),
					Messages.getString("ui.error.send_failure"));
		}
	}

	@FXML
	public void actionClear(ActionEvent event) {
		textAreaMessageToSend.clear();
	}

	@FXML
	public void actionFindContact(ActionEvent event) {

		List<ContactDto> result = showFindContactDialog(((Node) event.getSource()).getScene().getWindow());
		System.out.println("RESULT " + result.size());
		System.out.println(result);

		List<Conversation> resultToAdd = result.stream()
				.peek(dto -> System.out.println("DTO" + dto))
				.map(conversationService::createConversationIfNotExistsAndSave)
				.peek(o -> System.out.println("Opt" + o))
				.flatMap(Optional::stream)
				.peek(c -> System.out.println("Conv" + c))
				.toList();
		System.out.println("RESULT TO ADD " + resultToAdd);
		Platform.runLater(() -> conversations.addAll(resultToAdd));
	}

	private List<ContactDto> showFindContactDialog(Window owner) {

		var viewModel = new BasicViewModel<Void, List<ContactDto>>(null);

		Stage stage = new Stage();
		Parent parent = findContactView.getRootNode();
		findContactView.setViewModel(viewModel);

		final double width = 500;
		final double height = 500;

		Scene scene = new Scene(parent, width, height);

		stage.setScene(scene);
		stage.sizeToScene();
		stage.setMinWidth(400);
		stage.setMinHeight(400);
		stage.setTitle(Messages.getString("ui.dialog.find_contact")); //$NON-NLS-1$
		stage.initModality(Modality.WINDOW_MODAL);
		if (owner != null) {
			stage.initOwner(owner);
		}
		stage.setOnShown(event -> {
			Platform.runLater(() -> {
				stage.setWidth(width);
				stage.setHeight(height);
			});
		});
		stage.showAndWait();

		stage.setScene(null);
		scene.setRoot(new Group()); // удаляем имеющийся корневой узел из графа

		return viewModel.getResult();
	}

	//	private void printConversationsForTest() {
	//
	//		transactionTemplate.executeWithoutResult(status -> {
	//
	//			List<Conversation> conversations = conversationRepository.findAll();
	//			System.out.println("CONVERSATIONS:");
	//			conversations.forEach(c -> {
	//				System.out.println("\n\t" + c.toString());
	//				c.getMessages().forEach(m -> {
	//					System.out.println("\t\t" + m.toString());
	//				});
	//			});
	//			this.conversations.clear();
	//			this.conversations.addAll(conversations);
	//
	//		});
	//	}

}
