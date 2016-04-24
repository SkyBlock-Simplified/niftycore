package net.netcoding.niftycore.util;

import com.google.common.base.Joiner;
import net.netcoding.niftycore.minecraft.ChatColor;
import net.netcoding.niftycore.util.concurrent.ConcurrentMap;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A collection of string methods for easy string
 * formatting, concatenation, checking and converting.
 */
public class StringUtil {

	private static final transient ConcurrentMap<String, MessageFormat> MESSAGE_CACHE = new ConcurrentMap<>();

	/**
	 * Returns a formatted string using a cached {@link MessageFormat}.
	 *
	 * @param format to format objects with
	 * @param objects to be used for replacement
	 * @return a formatted string
	 */
	public static String format(String format, Object... objects) {
		if (!MESSAGE_CACHE.containsKey(format)) {
			MessageFormat messageFormat = null;
			String newFormat = RegexUtil.replace(format, RegexUtil.LOG_PATTERN, (ChatColor.RED + "$1" + ChatColor.GRAY));

			try {
				messageFormat = new MessageFormat(newFormat);
			} catch (IllegalArgumentException iaex) {
				newFormat = newFormat.replaceAll(RegexUtil.LOG_PATTERN.pattern(), "\\[$1\\]");

				try {
					messageFormat = new MessageFormat(newFormat);
				} catch (IllegalArgumentException ignore) { }
			}

			MESSAGE_CACHE.put(format, messageFormat);
		}

		MessageFormat messageFormat = MESSAGE_CACHE.get(format);
		return (messageFormat != null ? messageFormat.format(objects) : format);
	}

	/**
	 * Gets a concatenated string separated by nothing.
	 *
	 * @param pieces to concatenate into string
	 * @return concatenated string
	 */
	public static String implode(String[] pieces) {
		return implode(toList(pieces));
	}

	/**
	 * Gets a concatenated string separated by nothing.
	 *
	 * @param collection to concatenate into string
	 * @return concatenated string
	 */
	public static String implode(Collection<String> collection) {
		return implode("", collection);
	}

	/**
	 * Gets a concatenated string separated by {@code glue}.
	 *
	 * @param glue to separate pieces with
	 * @param pieces to concatenate into string
	 * @return concatenated string
	 */
	public static String implode(String glue, String[] pieces) {
		return implode(glue, toList(pieces));
	}

	/**
	 * Gets a concatenated string separated by {@code glue}
	 *
	 * @param glue to separate pieces with
	 * @param collection to concatenate into string
	 * @return concatenated string
	 */
	public static String implode(String glue, Collection<String> collection) {
		return implode(glue, collection, 0);
	}

	/**
	 * Gets a concatenated string separated by nothing,
	 * and starts at index {@code start}.
	 *
	 * @param pieces to concatenate into string
	 * @param start index to start concatenating
	 * @return concatenated string
	 */
	public static String implode(String[] pieces, int start) {
		return implode("", toList(pieces), start);
	}

	/**
	 * Gets a concatenated string separated by nothing,
	 * and starts at index {@code start}.
	 *
	 * @param collection to concatenate into string
	 * @param start index to start concatenating
	 * @return concatenated string
	 */
	public static String implode(Collection<String> collection, int start) {
		return implode("", collection, start);
	}

	/**
	 * Gets a concatenated string separated by {@code glue},
	 * and starts at index {@code start}.
	 *
	 * @param glue to separate pieces with
	 * @param pieces to concatenate into string
	 * @param start index to start concatenating
	 * @return concatenated string
	 */
	public static String implode(String glue, String[] pieces, int start) {
		return implode(glue, toList(pieces), start);
	}

	/**
	 * Gets a concatenated string separated by {@code glue},
	 * and starts at index {@code start}.
	 *
	 * @param glue to separate pieces with
	 * @param collection to concatenate into string
	 * @param start index to start concatenating
	 * @return concatenated string
	 */
	public static String implode(String glue, Collection<String> collection, int start) {
		return implode(glue, collection, start, -1);
	}

	/**
	 * Gets a concatenated string separated by nothing,
	 * starts at index {@code start} and ends at index {@code end}.
	 *
	 * @param pieces to concatenate into string
	 * @param start index to start concatenating
	 * @param end index to stop concatenating
	 * @return concatenated string
	 */
	public static String implode(String[] pieces, int start, int end) {
		return implode("", toList(pieces), start, end);
	}

	/**
	 * Gets a concatenated string separated by nothing,
	 * starts at index {@code start} and ends at index {@code end}.
	 *
	 * @param collection to concatenate into string
	 * @param start index to start concatenating
	 * @param end index to stop concatenating
	 * @return concatenated string
	 */
	public static String implode(Collection<String> collection, int start, int end) {
		return implode("", collection, start, end);
	}

	/**
	 * Gets a concatenated string separated by {@code glue},
	 * starts at index {@code start} and ends at index {@code end}.
	 *
	 * @param glue to separate pieces with
	 * @param pieces to concatenate into string
	 * @param start index to start concatenating
	 * @param end index to stop concatenating
	 * @return concatenated string
	 */
	public static String implode(String glue, String[] pieces, int start, int end) {
		return implode(glue, toList(pieces), start, end);
	}

	/**
	 * Gets a concatenated string separated by {@code glue},
	 * starts at index {@code start} and ends at index {@code end}.
	 *
	 * @param glue to separate pieces with
	 * @param collection to concatenate into string
	 * @param start index to start concatenating
	 * @param end index to stop concatenating
	 * @return concatenated string
	 */
	public static String implode(String glue, Collection<String> collection, int start, int end) {
		if (isEmpty(glue)) glue = "";
		if (ListUtil.isEmpty(collection)) throw new IllegalArgumentException("Collection cannot be empty!");
		if (start < 0) start = 0;
		if (start > collection.size()) throw new IndexOutOfBoundsException(String.format("Cannot access index %d out of %d total pieces!", start, collection.size()));
		if (end < 0) end = collection.size();
		if (end > collection.size()) throw new IndexOutOfBoundsException(String.format("Cannot access index %d out of %d total pieces!", end, collection.size()));
		List<String> pieces = new ArrayList<>(collection);
		List<String> newPieces = new ArrayList<>();

		for (int i = start; i < end; i++)
			newPieces.add(pieces.get(i));

		return Joiner.on(glue).join(newPieces);
	}

	/**
	 * Gets if the {@code value} is empty or null.
	 *
	 * @param value to check
	 * @return true if empty or null, otherwise false
	 */
	public static boolean isEmpty(CharSequence value) {
		return value == null || "".equals(value) || value.length() == 0;
	}

	/**
	 * Gets if the {@code value} is empty or null.
	 *
	 * @param value to check
	 * @return true if empty or null, otherwise false
	 */
	public static boolean isEmpty(String value) {
		return value == null || "".equals(value) || value.length() == 0;
	}

	/**
	 * Gets if the {@code value} is not empty.
	 *
	 * @param value to check
	 * @return true if not empty or null, otherwise false
	 */
	public static boolean notEmpty(CharSequence value) {
		return !isEmpty(value);
	}

	/**
	 * Gets if the {@code value} is not empty.
	 *
	 * @param value to check
	 * @return true if not empty or null, otherwise false
	 */
	public static boolean notEmpty(String value) {
		return !isEmpty(value);
	}

	/**
	 * Gets a split array of the {@code value} using {@code regex}.
	 *
	 * @param regex The delimiting regular expression.
	 * @param value The value to split.
	 * @return a split array using the specified regex
	 */
	public static String[] split(String regex, String value) {
		return isEmpty(value) ? new String[0] : value.split(regex);
	}

	/**
	 * Removes null from {@code value} and will either be an empty
	 * value or the original passed value.
	 *
	 * @param value to safely return
	 * @return value or empty string
	 */
	public static String stripNull(String value) {
		return isEmpty(value) ? "" : value;
	}

	/**
	 * Gets a list of the string array. If the array is empty (see {@link ListUtil#isEmpty(Collection)}),
	 * then an empty list is returned.
	 *
	 * @param array to check
	 * @return string array converted to string list
	 */
	public static List<String> toList(String... array) {
		return new ArrayList<>(Arrays.asList(ListUtil.isEmpty(array) ? new String[] {} : array));
	}

	/**
	 * Encodes unicode characters in a string.
	 *
	 * @param value String to encode
	 * @return Encoded unicode version of the string
	 */
	public static String escapeUnicode(String value) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < value.length(); i++) {
			int codePoint = Character.codePointAt(value, i);
			int charCount = Character.charCount(codePoint);

			if (charCount > 1) {
				i += charCount - 1;

				if (i >= value.length())
					throw new IllegalArgumentException("Truncated value unexpectedly!");
			}

			if (codePoint < 128)
				builder.appendCodePoint(codePoint);
			else
				builder.append(String.format("\\u%x", codePoint));
		}

		return builder.toString();
	}

}