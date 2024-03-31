package edu.alexey.messengerclient.view;

import java.time.format.DateTimeFormatter;

import edu.alexey.messengerclient.bundles.LocaleManager;
import edu.alexey.messengerclient.entities.Message;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class MessageListViewCell extends ListCell<Message> {

	final static DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("E, MMM dd HH:mm");

	private final AnchorPane root;
	private final VBox vbox;
	private final Label labelTimestamp;
	private final Label labelContent;

	public MessageListViewCell() {

		labelTimestamp = new Label();
		labelContent = new Label();
		labelContent.setWrapText(true);
		labelContent.setPrefWidth(350);
		labelTimestamp.setTextFill(Color.BLACK);
		labelContent.setTextFill(Color.BLACK);

		Group group = new Group(labelContent);

		VBox.setVgrow(group, Priority.ALWAYS);

		vbox = new VBox(8d, labelTimestamp, group);
		vbox.setPrefWidth(400);

		vbox.setPadding(new Insets(8d, 16d, 8d, 16d));
		vbox.setFillWidth(true);
		vbox.setAlignment(Pos.CENTER_LEFT);
		vbox.setStyle("-fx-background-radius: 16;"
				+ " -fx-background-color: WHITE;"
				+ " -fx-border-width: 1;"
				+ " -fx-border-color: DARKGREY;"
				+ " -fx-border-radius: 16;"
				+ " -fx-effect: dropshadow(GAUSSIAN, DARKGREY, 4.0, 0.0, 4.0, 4.0);");

		AnchorPane.setBottomAnchor(vbox, 10d);
		AnchorPane.setTopAnchor(vbox, 10d);

		root = new AnchorPane(vbox);
	}

	@Override
	protected void updateItem(Message item, boolean empty) {
		super.updateItem(item, empty);
		if (empty || item == null) {
			setGraphic(null);
			return;
		}

		if (item.isOwn()) {
			vbox.setAlignment(Pos.CENTER_RIGHT);
			AnchorPane.setRightAnchor(vbox, 8d);
			AnchorPane.setLeftAnchor(vbox, null);
		} else {
			vbox.setAlignment(Pos.CENTER_RIGHT);
			AnchorPane.setLeftAnchor(vbox, 8d);
			AnchorPane.setRightAnchor(vbox, null);
		}
		labelTimestamp.setText(item.getSentAt()
				.format(TIMESTAMP_FORMATTER.withLocale(LocaleManager.getCurrent().getLocale())));
		labelContent.setText(item.getContent());

		//		labelTimestamp.autosize();
		//		labelContent.autosize();
		//		vbox.autosize();

		setGraphic(root);

		//		Platform.runLater(() -> {
		//			labelTimestamp.autosize();
		//			labelContent.autosize();
		//			vbox.autosize();
		//		});
	}
}
