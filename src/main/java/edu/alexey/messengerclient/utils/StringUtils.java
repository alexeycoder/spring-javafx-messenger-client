package edu.alexey.messengerclient.utils;

public final class StringUtils {

	public static boolean isNullOrBlank(String str) {
		return str == null || str.isBlank();
	}

	public static String nullifyEmpty(String str) {
		if (str == null || str.isEmpty()) {
			return null;
		}
		return str;
	}

	public static String nullifyBlank(String str) {
		if (str == null || str.isBlank()) {
			return null;
		}
		return str;
	}
}
