package edu.alexey.messengerclient.viewmodel;

import edu.alexey.messengerclient.dto.SignupDto;
import edu.alexey.messengerclient.viewmodel.abstractions.ViewModel;

public class SignupViewModel implements ViewModel<SignupDto, SignupDto> {

	private final SignupDto input;
	private SignupDto result;

	public SignupViewModel(SignupDto input) {
		this.input = input;
	}

	@Override
	public SignupDto getInput() {
		return input;
	}

	@Override
	public void setResult(SignupDto result) {
		this.result = result;
	}

	@Override
	public SignupDto getResult() {
		return result;
	}

}
