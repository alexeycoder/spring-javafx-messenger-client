package edu.alexey.messengerclient.viewmodel.abstractions;

public interface ViewModel<T, R> {

	T getInput();

	void setResult(R result);

	R getResult();
}
