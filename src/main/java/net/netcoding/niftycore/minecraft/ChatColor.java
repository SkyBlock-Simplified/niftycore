package net.netcoding.niftycore.minecraft;

import net.netcoding.niftycore.util.RegexUtil;
import net.netcoding.niftycore.util.StringUtil;

import java.util.regex.Pattern;

public enum ChatColor {

	BLACK('0'),
	DARK_BLUE('1'),
	DARK_GREEN('2'),
	DARK_AQUA('3'),
	DARK_RED('4'),
	DARK_PURPLE('5'),
	GOLD('6'),
	GRAY('7'),
	DARK_GRAY('8'),
	BLUE('9'),
	GREEN('a'),
	AQUA('b'),
	RED('c'),
	LIGHT_PURPLE('d'),
	YELLOW('e'),
	WHITE('f'),
	MAGIC('k', true),
	BOLD('l', true),
	STRIKETHROUGH('m', true),
	UNDERLINE('n', true),
	ITALIC('o', true),
	RESET('r');

	public static final char COLOR_CHAR = '\u00a7';
	private final char code;
	private final boolean isFormat;
	private final String toString;

	ChatColor(char code) {
		this(code, false);
	}

	ChatColor(char code, boolean isFormat) {
		this.code = code;
		this.isFormat = isFormat;
		this.toString = new String(new char[] { COLOR_CHAR, code });
	}

    /**
     * Get the color represented by the specified code.
     *
     * @param code The code to search for.
     * @return The mapped color, or null if non exists.
     */
	public static ChatColor getByChar(char code) {
		for (ChatColor color : values()) {
			if (color.code == code)
				return color;
		}

		return null;
	}

	public boolean isColor() {
		return !this.isFormat() && this != RESET;
	}

	public boolean isFormat() {
		return this.isFormat;
	}

    /**
     * Strips the given message of all color and format codes
     *
     * @param value String to strip of color
     * @return A copy of the input string, without any coloring
     */
	public static String stripColor(String value) {
		return RegexUtil.strip(StringUtil.stripNull(value), RegexUtil.VANILLA_PATTERN);
	}

	public static String translateAlternateColorCodes(char altColorChar, String value) {
		Pattern replaceAltColor = Pattern.compile(StringUtil.format("(?<!{0}){0}([0-9a-fk-orA-FK-OR])", altColorChar));
		return RegexUtil.replaceColor(value, replaceAltColor);
	}

	@Override
	public String toString() {
		return this.toString;
	}

}