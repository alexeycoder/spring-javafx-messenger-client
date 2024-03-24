package edu.alexey.messengerclient.view.abstractions;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import edu.alexey.messengerclient.bundles.Lang;
import edu.alexey.messengerclient.bundles.LocaleManager;
import edu.alexey.messengerclient.bundles.Messages;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.extern.slf4j.Slf4j;

/**
 * Интеграция FXML-контроллеров в Spring-контекст.
 */
@Slf4j
public abstract class SpringFxmlView implements ApplicationContextAware {

	// "" -- означает, что fxml должны находиться в том же пакете, что и данный
	// класс.
	private static final String FXML_PACKAGE_PREFIX = "";

	protected final URL fxmlUrl;
	private final Supplier<ResourceBundle> resourcesSupplier;

	protected FXMLLoader fxmlLoader;
	protected Parent rootNode;
	protected Object controller;
	private Lang currentLang;
	//protected ObjectProperty<Object> controllerProperty;

	//	protected Pane parent;
	//	protected Pane rootPane;

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (this.applicationContext == null) {
			this.applicationContext = applicationContext;
		}
	}

	public SpringFxmlView(String fxmlName) {
		//		this.controllerProperty = new SimpleObjectProperty<Object>();
		this.fxmlUrl = getClass().getResource(FXML_PACKAGE_PREFIX + fxmlName + ".fxml");
		log.info("FXML URL: {}", fxmlUrl);
		this.resourcesSupplier = Messages::resourceBundle;

		LocaleManager.addLangChangeListener(evt -> {
			if (currentLang == null || LocaleManager.getCurrent() != currentLang) {
				invalidate();
			}
		});
	}

	private Object getControllerOfType(Class<?> type) {
		return this.applicationContext.getBean(type);
	}

	private void loadFxmlAndInjectControllerBean() {

		fxmlLoader = new FXMLLoader(fxmlUrl, resourcesSupplier.get());
		fxmlLoader.setControllerFactory(this::getControllerOfType);

		try {
			rootNode = fxmlLoader.load();
			controller = fxmlLoader.getController();
		} catch (IOException e) {
			log.error("", e);
			throw new RuntimeException("Error on attempt to load " + fxmlUrl.toString(), e);
		}
	}

	private void ensureViewReady(boolean doReload) {

		if (!doReload && fxmlLoader != null) {
			return;
		}

		loadFxmlAndInjectControllerBean();

		if (rootNode == null) {
			throw new IllegalStateException("View is not ready as expected to be.");
		}
	}

	public void invalidate() {
		fxmlLoader = null;
		//ensureViewReady(true);
	}

	public Parent getRootNode() {

		ensureViewReady(false);
		return rootNode;
	}

	public Object getController() {

		ensureViewReady(false);
		return controller; // controllerProperty.get();
	}

}
