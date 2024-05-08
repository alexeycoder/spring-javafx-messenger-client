package edu.alexey.messengerclient.viewmodel;

import java.util.function.Function;

import javafx.beans.property.ReadOnlyStringPropertyBase;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

public class ReadOnlyStringValueFactory<T>
		implements Callback<CellDataFeatures<T, String>, ObservableValue<String>> {

	private final String propertyName;
	private final Function<T, String> propertyValueMapper;

	public ReadOnlyStringValueFactory(String propertyName, Function<T, String> propertyValueMapper) {
		this.propertyName = propertyName;
		this.propertyValueMapper = propertyValueMapper;
	}

	@Override
	public ObservableValue<String> call(CellDataFeatures<T, String> param) {
		return new ReadOnlyStringPropertyBase() {

			@Override
			public String get() {
				return propertyValueMapper.apply(param.getValue());
			}

			@Override
			public String getName() {
				return propertyName;
			}

			@Override
			public Object getBean() {
				return param.getValue();
			}
		};
	}

}
