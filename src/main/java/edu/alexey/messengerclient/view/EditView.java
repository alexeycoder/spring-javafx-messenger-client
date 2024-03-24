package edu.alexey.messengerclient.view;

import org.springframework.beans.TypeMismatchException;
import org.springframework.stereotype.Component;

import edu.alexey.messengerclient.view.abstractions.SpringFxmlView;
import edu.alexey.messengerclient.viewmodel.UserViewModel;
import edu.alexey.messengerclient.viewmodel.UserViewModelConsumer;

@Component
public class EditView extends SpringFxmlView {

	public EditView() {
		super("edit");
	}

	public void setViewModel(UserViewModel viewModel) {
		if (super.controller instanceof UserViewModelConsumer viewModelConsumer) {
			viewModelConsumer.accept(viewModel);
		} else {
			throw new TypeMismatchException(this.controller, UserViewModelConsumer.class);
		}
	}

}
