package edu.alexey.messengerclient.utils;

import java.lang.reflect.Method;
import java.util.Objects;

import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableColumnHeader;

public final class TableViewUtils {

	/**
	 * Для работы метода необходим доступ к пакету javafx.scene.control.skin модуля
	 * javafx.controls, для чего потребуется сделать пакет
	 * javafx.controls/javafx.scene.control.skin открытым для всех не-именованных
	 * модулей (если само приложение не модульное).
	 * 
	 * @param tableView
	 * @param columnId
	 */
	public static void resizeColumn(TableView<?> tableView, String columnId) {
		Objects.requireNonNull(tableView);
		Objects.requireNonNull(columnId);

		var headers = tableView.lookupAll("TableColumnHeader");
		if (headers != null) {
			TableColumnHeader tableColumnHeader = headers.stream()
					.map(h -> (h instanceof TableColumnHeader tch) ? tch : null)
					.filter(Objects::nonNull)
					.filter(t -> columnId.equals(t.getId()))
					.findAny().orElse(null);
			if (tableColumnHeader != null) {
				Method method;
				try {
					method = tableColumnHeader.getClass().getDeclaredMethod("resizeColumnToFitContent", int.class);
					method.setAccessible(true);
					method.invoke(tableColumnHeader, 30);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

}
