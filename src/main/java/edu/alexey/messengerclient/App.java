package edu.alexey.messengerclient;

import java.beans.PropertyChangeEvent;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import edu.alexey.messengerclient.bundles.LocaleManager;
import edu.alexey.messengerclient.bundles.Messages;
import edu.alexey.messengerclient.preloader.BasicPreloader;
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

@SpringBootApplication
public class App extends JavaFxAsSpringBeanApplication {

	@Autowired
	private MainView mainView;

	private Stage primaryStage;
	private VBox root;
	//private final Disposer disposer = new Disposer();

	public static void main(String[] args) {

		System.setProperty("javafx.preloader", BasicPreloader.class.getCanonicalName());
		launchSpringJavaFxApp(App.class, args);
	}

	@Override
	public void init() throws Exception {
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
	public void start(Stage stage) throws Exception {

		primaryStage = stage;

		root = (VBox) mainView.getRootNode();
		Scene scene = new Scene(root, 720, 480);

		stage.setTitle(Messages.getString("app_name")); //$NON-NLS-1$
		stage.setMinWidth(600);
		stage.setMinHeight(400);

		stage.setScene(scene);
		stage.sizeToScene();
		stage.centerOnScreen();
		stage.setOnShown(event -> {
			new Thread(() -> {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					return;
				}
				Platform.runLater(stage::centerOnScreen);
			}).start();
		});
		stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEventFilter);
		stage.setOnHidden(this::closeWindowEventHandler);

		stage.show();

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
		//		disposer.releaseResources();
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		System.exit(0);
	}
}
