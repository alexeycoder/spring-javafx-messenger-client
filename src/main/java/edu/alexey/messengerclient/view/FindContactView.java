package edu.alexey.messengerclient.view;

import java.util.List;

import org.springframework.beans.TypeMismatchException;
import org.springframework.stereotype.Component;

import edu.alexey.messengerclient.dto.ContactDto;
import edu.alexey.messengerclient.view.abstractions.SpringFxmlView;
import edu.alexey.messengerclient.viewmodel.FindContactViewModelConsumer;
import edu.alexey.messengerclient.viewmodel.abstractions.ViewModel;

@Component
public class FindContactView extends SpringFxmlView {

	public FindContactView() {
		super("find");
	}

	public void setViewModel(ViewModel<Void, List<ContactDto>> viewModel) {
		if (super.controller instanceof FindContactViewModelConsumer viewModelConsumer) {
			viewModelConsumer.accept(viewModel);
		} else {
			throw new TypeMismatchException(this.controller, FindContactViewModelConsumer.class);
		}
	}
}
