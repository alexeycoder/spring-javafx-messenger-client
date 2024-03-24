package edu.alexey.messengerclient.view;

import org.springframework.stereotype.Component;

import edu.alexey.messengerclient.view.abstractions.SpringFxmlView;

@Component
public class MainView extends SpringFxmlView {

	public MainView() {
		super("main");
	}

}
