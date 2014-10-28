package com.glogo.wikiparser;

import java.util.Date;

/**
 * Simple logging class for printing info and error statements.
 * Prepends current date time into printed string
 * @author Glogo
 */
public class Logger {
	
	private static String getCurrentDateTimePrefix() {
		return String.format("[%s] ", new Date().toString());
	}
	
	public static void info(String format, Object ... args) {
		System.out.println(getCurrentDateTimePrefix() + String.format(format, args));
	}
	
	public static void error(String format, Object ... args) {
		System.err.println(getCurrentDateTimePrefix() + String.format(format, args));
	}
}
