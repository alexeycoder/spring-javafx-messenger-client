package edu.alexey.messengerclient.viewmodel.abstractions;

import java.util.function.Consumer;

public interface ViewModelConsumer<T, R> extends Consumer<ViewModel<T, R>> {
}
