package edu.alexey.messengerclient.view;

import edu.alexey.messengerclient.entities.Conversation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

public class ConversationListViewCell extends ListCell<Conversation> {

	//	@Override
	//	protected void updateItem(Conversation item, boolean empty) {
	//		super.updateItem(item, empty);
	//		if (empty || item == null) {
	//			setText(null);
	//		} else {
	//			setText("\u263a " + item.getContact().getDisplayName());
	//		}
	//	}

	private final VBox root;
	private final Label labelDisplayName;
	private final Label labelUuid;

	public ConversationListViewCell() {
		labelDisplayName = new Label(null, new Circle(6, Color.LIGHTBLUE));
		labelUuid = new Label();
		Font font = labelUuid.getFont();
		labelUuid.setFont(new Font(font.getName(), font.getSize() - 2));
		root = new VBox(0, labelDisplayName, labelUuid);
		root.setAlignment(Pos.CENTER_LEFT);

		this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
			ConversationListViewCell src = (ConversationListViewCell) event.getSource();
			Parent prn = src.getParent();
			while (prn != null) {
				if (prn instanceof ListView lv) {
					if (src.isEmpty() || src.isSelected()) {
						lv.getSelectionModel().clearSelection();
						event.consume();
					}
					break;
				}
				prn = prn.getParent();
			}
		});
	}

	@Override
	protected void updateItem(Conversation item, boolean empty) {
		super.updateItem(item, empty);
		if (empty || item == null) {
			setGraphic(null);
		} else {
			labelDisplayName.setText(item.getContact().getDisplayName());
			labelUuid.setText(item.getContact().getUserUuid().toString());
			setGraphic(root);
		}
	}
}
