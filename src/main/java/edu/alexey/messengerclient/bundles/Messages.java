package edu.alexey.messengerclient.bundles;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

public final class Messages {

	private Messages() {
	}

	public static final String BUNDLE_NAME = Messages.class.getPackageName() + ".Locale"; //$NON-NLS-1$

	private volatile static ResourceBundle resourceBundle;
	private volatile static Locale resourceBundleLocale;

	public static ResourceBundle resourceBundle() {
		if (resourceBundle == null) {
			actualizeResources();
		}
		return resourceBundle;
	}

	public static void actualizeResources() {
		Lang lang = LocaleManager.getCurrent();

		Locale locale = lang != null ? lang.getLocale() : Locale.getDefault();
		if (!Objects.equals(locale, resourceBundleLocale)) {
			if (locale == null) {
				locale = Locale.getDefault();
			}
			Messages.resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
			resourceBundleLocale = locale;
		}
	}

	public static String getString(String key) {
		try {
			return resourceBundle().getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

}
