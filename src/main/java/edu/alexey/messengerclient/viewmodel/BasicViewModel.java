package edu.alexey.messengerclient.viewmodel;

import edu.alexey.messengerclient.viewmodel.abstractions.ViewModel;

public class BasicViewModel<T, R> implements ViewModel<T, R> {

	private final T input;
	private R result;

	public BasicViewModel(T input) {
		this.input = input;
	}

	@Override
	public T getInput() {
		return input;
	}

	@Override
	public void setResult(R result) {
		this.result = result;
	}

	@Override
	public R getResult() {
		return result;
	}

}
