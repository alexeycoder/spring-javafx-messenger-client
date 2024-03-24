package edu.alexey.messengerclient.preloader;

import java.util.Locale;

import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BasicPreloader extends Preloader {

	private Stage stage;
	private Scene scene;
	private ProgressBar progressBar;

	@Override
	public void start(Stage primaryStage) throws Exception {

		Label title = new Label(Locale.getDefault().equals(Locale.ENGLISH) ? "Loading..." : "Загрузка...");
		title.setTextAlignment(TextAlignment.CENTER);
		progressBar = new ProgressBar();
		progressBar.setMinWidth(250);
		VBox root = new VBox(title, progressBar);
		root.setAlignment(Pos.CENTER);
		scene = new Scene(root, 250, 150);

		stage = primaryStage;
		stage.initStyle(StageStyle.UNDECORATED);
		stage.setScene(scene);
		stage.show();
	}

	@Override
	public void handleApplicationNotification(PreloaderNotification info) {
		if (info instanceof ProgressNotification progress) {
			double progressValue = progress.getProgress();
			if (progressValue >= 1d) {
				stage.close();
			} else {
				progressBar.setProgress(progress.getProgress());
			}
		}
	}

	@Override
	public void handleStateChangeNotification(StateChangeNotification info) {
		var type = info.getType();
		switch (type) {
		case BEFORE_LOAD:
			log.info("Preloader: BEFORE LOAD");
			break;
		case BEFORE_INIT:
			log.info("Preloader: BEFORE INIT");
			break;
		case BEFORE_START:
			log.info("Preloader: BEFORE START - Close preloader stage");
			stage.close();
			break;
		default:
			throw new IllegalArgumentException("Unexpected value: " + type);
		}
	}

}
