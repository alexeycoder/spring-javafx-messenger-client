package edu.alexey.messengerclient;

import java.beans.PropertyChangeEvent;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;

import edu.alexey.messengerclient.bundles.LocaleManager;
import edu.alexey.messengerclient.bundles.Messages;
import edu.alexey.messengerclient.preloader.BasicPreloader;
import edu.alexey.messengerclient.utils.CustomProperties;
import edu.alexey.messengerclient.utils.DialogManager;
import edu.alexey.messengerclient.view.MainView;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Lazy
@SpringBootApplication
public class App extends JavaFxAsSpringBeanApplication {

	@Autowired
	private MainView mainView;
	@Autowired
	private CustomProperties customProperties;

	private Stage primaryStage;
	private VBox root;

	public static void main(String[] args) {

		System.out.println(
				"MAIN THREAD: " + Thread.currentThread().getName() + " ID = " + Thread.currentThread().threadId());

		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		System.setProperty("javafx.preloader", BasicPreloader.class.getCanonicalName());
		launchSpringJavaFxApp(App.class, args);
	}

	@Override
	public void init() {

		System.out.println(
				"INIT App THREAD: " + Thread.currentThread().getName() + " ID = " + Thread.currentThread().threadId());
		// Имитация инициализации приложения
		new Thread(() -> {
			for (int i = 0; i <= 100; ++i) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					return;
				}
				notifyPreloader(new Preloader.ProgressNotification(i / 100d));
			}
		}).start();

		super.init();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		this.primaryStage = primaryStage;

		System.out.println(
				"START THREAD: " + Thread.currentThread().getName() + " ID = " + Thread.currentThread().threadId());

		LocaleManager.setCurrent(customProperties.getLanguage());

		root = (VBox) mainView.getRootNode();
		Scene scene = new Scene(root, 900, 600);

		primaryStage.setTitle(Messages.getString("app_name")); //$NON-NLS-1$
		primaryStage.setMinWidth(600);
		primaryStage.setMinHeight(400);

		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		primaryStage.centerOnScreen();
		primaryStage.setOnShown(event -> {
			new Thread(() -> {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					return;
				}
				Platform.runLater(() -> {
					primaryStage.setWidth(900);
					primaryStage.setHeight(600);
					primaryStage.centerOnScreen();
				});
			}).start();
		});
		primaryStage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEventFilter);
		primaryStage.setOnHidden(this::closeWindowEventHandler);

		primaryStage.show();

		LocaleManager.addLangChangeListener(this::reload);
	}

	private void reload(PropertyChangeEvent event) {

		mainView.invalidate();
		var newRoot = mainView.getRootNode();
		root.getChildren().setAll(newRoot.getChildrenUnmodifiable());
		primaryStage.setTitle(Messages.getString("app_name"));
	}

	private void closeWindowEventFilter(WindowEvent event) {
		System.out.println("Window close requested...");
		try {
			customProperties.save();
		} catch (Exception e) {
			log.error("Error on custom properties save.", e);
			DialogManager.showErrorDialog(
					Messages.getString("ui.error"),
					Messages.getString("ui.error.unable_save_config"));
		}

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.getButtonTypes().removeAll(ButtonType.OK);
		alert.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.YES);
		alert.setTitle(Messages.getString("ui.main.exit_confirmation_title")); //$NON-NLS-1$
		alert.setHeaderText(null);
		alert.setGraphic(null);
		alert.setContentText(Messages.getString("ui.main.exit_confirmation_text")); //$NON-NLS-1$
		alert.initOwner(primaryStage);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent()) {
			if (result.get().equals(ButtonType.CANCEL)) {
				event.consume(); // предотвращает закрытие приложения
			}
		}
	}

	private void closeWindowEventHandler(WindowEvent event) {

	}

	@Override
	public void stop() {
		super.stop();
		System.exit(0);
	}
}
