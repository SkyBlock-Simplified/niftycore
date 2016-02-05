package net.netcoding.niftycore.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A collection of number utilities to assist in number checking,
 * random number generating as well as {@link DataUtil#readVarInt(DataInputStream) readVarInt}
 * and {@link DataUtil#writeVarInt(DataOutputStream, int) writeVarInt} used in bukkits network protocols.
 */
public class NumberUtil {

	/**
	 * Gets if {@code value} is a valid number.
	 *
	 * @param value the value to check
	 * @return true if the value can be casted to a number, otherwise false
	 */
	public static boolean isInt(String value) {
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition position = new ParsePosition(0);
		formatter.parse(value, position);
		return value.length() == position.getIndex();
	}

	/**
	 * Gets a truely random number.
	 *
	 * @param minimum the lowest number allowed
	 * @return a random integer between the specified boundaries
	 */
	public static int rand(int minimum) {
		return rand(minimum, Integer.MAX_VALUE);
	}

	/**
	 * Gets a truely random number.
	 *
	 * @param minimum the lowest number allowed
	 * @param maximum the highest number allowed
	 * @return a random integer between the specified boundaries
	 */
	public static int rand(int minimum, int maximum) {
		return ThreadLocalRandom.current().nextInt(minimum, maximum + (maximum < Integer.MAX_VALUE ? 1 : 0));
	}

	/**
	 * Rounds number to nearest multipleOf value
	 *
	 * @param number the number to round
	 * @param multipleOf multiple to round to
	 * @return rounded version of number
	 */
	public static int round(double number, int multipleOf) {
		return (int)(Math.round(number / multipleOf) * multipleOf);
	}

	/**
	 * Rounds number up to nearest multipleOf value
	 *
	 * @param number the number to round up
	 * @param multipleOf multiple to round to
	 * @return rounded up version of number
	 */
	public static int roundUp(double number, int multipleOf) {
		return (int)(Math.ceil(number / multipleOf) * multipleOf);
	}

	/**
	 * Rounds number down to nearest multipleOf value
	 *
	 * @param number the number to round down
	 * @param multipleOf multiple to round to
	 * @return rounded down version of number
	 */
	public static int roundDown(double number, int multipleOf) {
		return (int)(Math.floor(number / multipleOf) * multipleOf);
	}

}