package net.netcoding.nifty.core.util;

import net.netcoding.nifty.core.reflection.Reflection;

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

	private static final NumberFormat FORMATTER = NumberFormat.getInstance();

	public static int ceil(double num) {
		int floor = (int)num;
		return (((double)floor == num) ? floor : (floor + (int)(~Double.doubleToRawLongBits(num) >>> 63)));
	}

	public static int floor(double num) {
		int floor = (int)num;
		return (((double)floor == num) ? floor : (floor - (int)(Double.doubleToRawLongBits(num) >>> 63)));
	}

	public static boolean isFinite(double d) {
		return Math.abs(d) <= 1.7976931348623157E308D;
	}

	public static boolean isFinite(float f) {
		return Math.abs(f) <= 3.4028235E38F;
	}

	/**
	 * Gets if {@code value} is a valid number.
	 *
	 * @param value the value to check
	 * @return true if the value can be casted to a number, otherwise false
	 */
	public static boolean isNumber(String value) {
		ParsePosition position = new ParsePosition(0);
		FORMATTER.parse(value, position);
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
		return ThreadLocalRandom.current().nextInt(minimum, maximum + 1);
	}

	public static int round(double num) {
		return floor(num + 0.5D);
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

	public static double square(double num) {
		return num * num;
	}

	public static <N extends Number> N to(Object value, Class<N> clazz) {
		Reflection number = new Reflection(clazz);
		String numValue = String.valueOf(value);
		return clazz.cast(number.newInstance(isNumber(numValue) ? numValue : String.valueOf(0)));
	}

	/**
	 * Gets the hexadecimal string of an integer.
	 *
	 * @param number to convert
	 * @return converted byte array as hexadecimal string
	 */
	public static String toHexString(long number) {
		return Long.valueOf(String.valueOf(number), 16).toString();
	}

	/**
	 * Gets the base 10 representation of the specified hexadecimal string.
	 *
	 * @param hexString Hexadecimal string to convert.
	 * @return Base 10 version.
	 */
	public static Long toLong(String hexString) {
		return Long.parseLong(hexString, 16);
	}

}