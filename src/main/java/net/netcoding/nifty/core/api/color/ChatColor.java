package net.netcoding.nifty.core.api.color;

import com.google.common.base.Preconditions;
import net.netcoding.nifty.core.util.RegexUtil;
import net.netcoding.nifty.core.util.StringUtil;

import java.util.regex.Pattern;

public enum ChatColor {

	BLACK('0', 0x000000),
	DARK_BLUE('1', 0x0000AA),
	DARK_GREEN('2', 0x00AA00),
	DARK_AQUA('3', 0x00AAAA),
	DARK_RED('4', 0xAA0000),
	DARK_PURPLE('5', 0xAA00AA),
	GOLD('6', 0xFFAA00),
	GRAY('7', 0xAAAAAA),
	DARK_GRAY('8', 0x555555),
	BLUE('9', 0x5555FF),
	GREEN('a', 0x55FF55),
	AQUA('b', 0x55FFFF),
	RED('c', 0xFF5555),
	LIGHT_PURPLE('d', 0xFF55FF),
	YELLOW('e', 0xFFFF55),
	WHITE('f', 0xFFFFFF),
	MAGIC('k', true, "obfuscated"),
	BOLD('l', true),
	STRIKETHROUGH('m', true),
	UNDERLINE('n', true, "underlined"),
	ITALIC('o', true),
	RESET('r');

	public static final char COLOR_CHAR = '\u00a7';
	private final char code;
	private final boolean isFormat;
	private final String jsonName;
	private final String toString;
	private final Color color;

	ChatColor(char code) {
		this(code, -1);
	}

	ChatColor(char code, int rgb) {
		this(code, false, rgb);
	}

	ChatColor(char code, boolean isFormat) {
		this(code, isFormat, -1);
	}

	ChatColor(char code, boolean isFormat, int rgb) {
		this(code, isFormat, null, rgb);
	}

	ChatColor(char code, boolean isFormat, String jsonName) {
		this(code, isFormat, jsonName, -1);
	}

	ChatColor(char code, boolean isFormat, String jsonName, int rgb) {
		this.code = code;
		this.isFormat = isFormat;
		this.jsonName = jsonName;
		this.toString = new String(new char[] { COLOR_CHAR, code });
		this.color = (rgb > -1 ? Color.fromRGB(rgb) : null);
	}

	public int asRGB() {
		return this.getColor().asRGB();
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

	public char getCode() {
		return this.code;
	}

	public Color getColor() {
		Preconditions.checkArgument(this.isColor(), "Format has no color!");
		return this.color;
	}

	public String getJsonName() {
		return StringUtil.isEmpty(this.jsonName) ? this.name().toLowerCase() : this.jsonName;
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