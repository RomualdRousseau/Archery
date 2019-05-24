package com.github.romualdrousseau.any2json.util;

import java.util.regex.Pattern;

public class StringUtility
{
	public static final String WHITE_SPACES = "\\s\\u00A0\\u3000";

    public static final String WRONG_UNICODE = "\\uFFFD";

    public static boolean isEmpty(String s) {
        return s == null || StringUtility.trim(s, StringUtility.WHITE_SPACES).equals("");
    }

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

	public static String singleWhiteSpaces(String s) {
		return singleWhiteSpaces(s, StringUtility.WHITE_SPACES);
	}

	public static String singleWhiteSpaces(String s, String whiteSpaces) {
		if(s == null) {
			return null;
		}
		return s.replaceAll("[" + whiteSpaces + "]+", " ");
	}

	public static boolean checkIfGoodEncoding(String s) {
		return !Pattern.compile(StringUtility.WRONG_UNICODE).matcher(s).find();
	}

	public static String cleanHeaderToken(String token) {
        //token = token.replaceAll("\\(.*\\)", "");
		//token = token.replaceAll("/.*", "");
        //token = token.replaceAll("▲", "");
        token = StringUtility.normalizeWhiteSpaces(token);
		token = StringUtility.singleWhiteSpaces(token);
		token = StringUtility.trim(token, StringUtility.WHITE_SPACES + "\"");
		return token;
	}

	public static String cleanValueToken(String token) {
		token = StringUtility.normalizeWhiteSpaces(token);
		token = StringUtility.singleWhiteSpaces(token);
		token = StringUtility.trim(token, StringUtility.WHITE_SPACES + "\"");
		return token;
	}
}
