package de.fh.aachen.bpmn.rest.impl.util;

public class StringUtils {
	public static boolean isEmptyOrNull(String input) {
		return (input == null) || input.equals("");
	}
	
	public static boolean isNotEmptyOrNull(String input) {
		return !((input == null) || input.equals(""));
	}
}
