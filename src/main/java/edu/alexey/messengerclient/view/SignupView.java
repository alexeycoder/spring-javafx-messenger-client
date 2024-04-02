package edu.alexey.messengerclient.view;

import org.springframework.beans.TypeMismatchException;
import org.springframework.stereotype.Component;

import edu.alexey.messengerclient.dto.SignupDto;
import edu.alexey.messengerclient.view.abstractions.SpringFxmlView;
import edu.alexey.messengerclient.viewmodel.SignupViewModelConsumer;
import edu.alexey.messengerclient.viewmodel.abstractions.ViewModel;

@Component
public class SignupView extends SpringFxmlView {

	public SignupView() {
		super("signup");
	}

	public void setViewModel(ViewModel<SignupDto, SignupDto> viewModel) {
		if (super.controller instanceof SignupViewModelConsumer viewModelConsumer) {
			viewModelConsumer.accept(viewModel);
		} else {
			throw new TypeMismatchException(this.controller, SignupViewModelConsumer.class);
		}
	}

}
