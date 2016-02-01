package net.netcoding.niftycore.util;

import net.netcoding.niftycore.util.comparator.LastCharCompare;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

	private static final MaxSizeHashMap<String, String> ORDERED_MESSAGES = new MaxSizeHashMap<>(50);
	private static transient LastCharCompare CODE_COMPARE = new LastCharCompare();
	private static final transient String ALL_PATTERN = "[0-9A-FK-ORa-fk-or]";
	public static final transient String SECTOR_SYMBOL = "\u00a7";
	public static final transient Pattern VANILLA_PATTERN = Pattern.compile(SECTOR_SYMBOL + "+" + ALL_PATTERN + "?");
	public static final transient Pattern VANILLA_COLOR_PATTERN = Pattern.compile(SECTOR_SYMBOL + "+[0-9A-Fa-f]");
	public static final transient Pattern VANILLA_MAGIC_PATTERN = Pattern.compile(SECTOR_SYMBOL + "+[Kk]");
	public static final transient Pattern VANILLA_FORMAT_PATTERN = Pattern.compile(SECTOR_SYMBOL + "+[L-ORl-or]");

	public static final transient Pattern REPLACE_ALL_PATTERN = Pattern.compile("(?<!&)&(" + ALL_PATTERN + ")");
	public static final transient Pattern REPLACE_COLOR_PATTERN = Pattern.compile("(?<!&)&([0-9a-fA-F])");
	public static final transient Pattern REPLACE_MAGIC_PATTERN = Pattern.compile("(?<!&)&([Kk])");
	public static final transient Pattern REPLACE_FORMAT_PATTERN = Pattern.compile("(?<!&)&([l-orL-OR])");
	private static final transient Pattern REPLACE_PATTERN = Pattern.compile("&&(?=" + ALL_PATTERN + ")");

	public static final transient Pattern LOG_PATTERN = Pattern.compile("\\{(\\{[\\d]+(?:,[^,\\}]+)*\\}.*?)\\}");
	public static final transient Pattern URL_PATTERN = Pattern.compile("((?:(?:https?)://)?[\\w\\._-]{2,})\\.([a-z]{2,6}(?:/\\S+)?)");
	public static final transient Pattern URL_FILTER_PATTERN = Pattern.compile("((?:(?:https?)://)?[\\w\\._-]{2,})\\.([a-z]{2,6}(?:(?::\\d+)?/\\S+)?)");
	public static final transient Pattern IP_FILTER_PATTERN = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(?::\\d*)?)");
	public static final transient Pattern IP_VALIDATE_PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	static {
		CODE_COMPARE.addIgnoreCharacter('r');
	}

	public static String replace(String message, Pattern pattern) {
		return replace(message, pattern, "$1");
	}

	public static String replace(String message, Pattern pattern, String replace) {
		return pattern.matcher(message).replaceAll(replace);
	}

	public static String replaceColor(String message, Pattern pattern) {
		if (!ORDERED_MESSAGES.containsKey(message)) {
			Pattern patternx = Pattern.compile(StringUtil.format("(((?:[&{0}]{1}){2})+)([^&{0}]*)", SECTOR_SYMBOL, "{1,2}", ALL_PATTERN));
			String[] parts = StringUtil.split(" ", message);
			String newMessage = message;

			for (String part : parts) {
				Matcher matcher = patternx.matcher(part);
				String newPart = part;

				while (matcher.find()) {
					String[] codes = matcher.group(1).split(StringUtil.format("(?<!&|{0})", SECTOR_SYMBOL));
					Arrays.sort(codes, CODE_COMPARE);
					String replace = StringUtil.format("{0}{1}", StringUtil.implode(codes), matcher.group(3));
					newPart = newPart.replace(matcher.group(0), replace);
				}

				newMessage = newMessage.replace(part, newPart);
			}

			ORDERED_MESSAGES.put(message, newMessage);
		}

		return replace(replace(ORDERED_MESSAGES.get(message), pattern, RegexUtil.SECTOR_SYMBOL + "$1"), REPLACE_PATTERN, "&");
	}

	public static String strip(String message, Pattern pattern) {
		return replace(message, pattern, "");
	}

	private static class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V> {

		private final int maxSize;

		public MaxSizeHashMap() {
			this(Integer.MAX_VALUE);
		}

		public MaxSizeHashMap(int maxSize) {
			this.maxSize = maxSize;
		}

		public boolean removeEldestEntry() {
			return this.removeEldestEntry(null);
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			return this.size() > this.maxSize;
		}

	}

}