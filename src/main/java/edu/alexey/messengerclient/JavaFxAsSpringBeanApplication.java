package edu.alexey.messengerclient;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;

// Класс интеграции JavaFX-приложения и Spring
public abstract class JavaFxAsSpringBeanApplication extends Application {

	// Аргументы, указанные при старте приложения
	private static String[] args;

	protected static void launchSpringJavaFxApp(
			Class<? extends JavaFxAsSpringBeanApplication> appClass,
			String[] args) {

		JavaFxAsSpringBeanApplication.args = args;
		Application.launch(appClass, args);
	}

	// Spring-контекст приложения, который связываем с JavaFX-контекстом
	protected ConfigurableApplicationContext applicationContext;

	@Override
	public void init() {

		System.out.println(
				"INIT THREAD: " + Thread.currentThread().getName() + " ID = " + Thread.currentThread().threadId());

		applicationContext = SpringApplication.run(getClass(), args);

		// Присоединяем экземпляр JavaFX Application, стартующий JavaFX-приложение,
		// и который пока ещё не является Spring-бином, к общему контексту Spring.
		// После чего JavaFX-сущность приложения будет представлена регулярным бином,
		// управляемым Spring Framework'ом привычным образом, и станет возможно
		// внедрять данный бин в другие сущности общего контекста.
		applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
	}

	@Override
	public void stop() {

		System.out.println(
				"STOP THREAD: " + Thread.currentThread().getName() + " ID = " + Thread.currentThread().threadId());

		applicationContext.close();
	}
}
