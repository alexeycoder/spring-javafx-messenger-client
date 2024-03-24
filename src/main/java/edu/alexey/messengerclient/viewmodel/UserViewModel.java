package edu.alexey.messengerclient.viewmodel;

import edu.alexey.messengerclient.entities.User;
import edu.alexey.messengerclient.viewmodel.abstractions.ViewModel;

public class UserViewModel implements ViewModel<User, User> {

	private final User input;
	private User result;

	public UserViewModel(User input) {
		this.input = input;
	}

	@Override
	public User getInput() {
		return input;
	}

	@Override
	public void setResult(User result) {
		this.result = result;
	}

	@Override
	public User getResult() {
		return result;
	}

}
