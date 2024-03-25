package edu.alexey.messengerclient.bundles;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class LocaleManager {

	private static final PropertyChangeSupport pcs = new PropertyChangeSupport(LocaleManager.class);

	public static final List<Lang> LANGUAGES = List.of(
			new Lang(0, "ru", "lang_ru", Locale.of("ru")),
			new Lang(1, "en", "lang_en", Locale.ENGLISH));

	private static Lang current = LANGUAGES.get(0);

	public static Lang getCurrent() {
		return current;
	}

	public static void setCurrent(Lang lang) {
		if (!Objects.equals(lang, current)) {
			var oldValue = current;
			LocaleManager.current = lang != null ? lang : LANGUAGES.get(0);
			Locale.setDefault(getCurrent().getLocale());
			Messages.actualizeResources();
			pcs.firePropertyChange("current", oldValue, current);
		}
	}

	public static void setCurrent(String code) {
		Optional<Lang> langOpt = LANGUAGES.stream().filter(l -> l.getCode().equalsIgnoreCase(code)).findAny();
		if (langOpt.isPresent()) {
			current = langOpt.get();
		} else {
			throw new IllegalArgumentException();
		}
	}

	public static void addLangChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public static void removeLangChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
}
