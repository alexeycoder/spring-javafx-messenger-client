package edu.alexey.messengerclient.bundles;

import java.util.Locale;

public class Lang {

	private String code;
	private String nameKey;
	private Locale locale;
	private int index;

	public Lang(int index, String code, String nameKey, Locale locale) {
		this.index = index;
		this.code = code;
		this.nameKey = nameKey;
		this.locale = locale;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return Messages.getString(nameKey);
	}

	public Locale getLocale() {
		return locale;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return getName();
	}

}
