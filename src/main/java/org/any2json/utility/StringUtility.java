package org.any2json.utility;

import java.util.regex.Pattern;

public class StringUtility
{
	public static final String WHITE_SPACES = "\\s\\u00A0\\u3000";

	public static final String WRONG_UNICODE = "\\uFFFD";

	public static String trim(String s) {
		return trim(s, StringUtility.WHITE_SPACES);
	}

	public static String trim(String s, String whiteSpaces) {
		if(s == null) {
			return null;
		}
		return s.replaceAll("^[" + whiteSpaces + "]+", "").replaceAll("[" +whiteSpaces + "]+$", "");
	}

	public static String normalizeWhiteSpaces(String s) {
		return normalizeWhiteSpaces(s, StringUtility.WHITE_SPACES);
	}

	public static String normalizeWhiteSpaces(String s, String whiteSpaces) {
		if(s == null) {
			return null;
		}
		return s.replaceAll("[" + whiteSpaces + "]", " ");
	}

	public static String removeWhiteSpaces(String s) {
		return removeWhiteSpaces(s, StringUtility.WHITE_SPACES);
	}

	public static String removeWhiteSpaces(String s, String whiteSpaces) {
		if(s == null) {
			return null;
		}
		return s.replaceAll("[" + whiteSpaces + "]", "");
	}

	public static boolean checkIfGoodEncoding(String s) {
		return !Pattern.compile(StringUtility.WRONG_UNICODE).matcher(s).find();
	}
}