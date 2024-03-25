package edu.alexey.messengerclient.viewmodel;

import edu.alexey.messengerclient.entities.Contact;
import edu.alexey.messengerclient.viewmodel.abstractions.ViewModel;

public class UserViewModel implements ViewModel<Contact, Contact> {

	private final Contact input;
	private Contact result;

	public UserViewModel(Contact input) {
		this.input = input;
	}

	@Override
	public Contact getInput() {
		return input;
	}

	@Override
	public void setResult(Contact result) {
		this.result = result;
	}

	@Override
	public Contact getResult() {
		return result;
	}

}
