package net.netcoding.nifty.core.util.comparator;

import net.netcoding.nifty.core.util.concurrent.Concurrent;
import net.netcoding.nifty.core.util.concurrent.ConcurrentSet;

import java.util.Comparator;

public class LastCharCompare implements Comparator<String> {

	private final ConcurrentSet<Character> ignoreCharacters = Concurrent.newSet();

	public void addIgnoreCharacter(char c) {
		this.ignoreCharacters.add(c);
	}

	@Override
	public int compare(String s1, String s2) {
		if (s1.length() == 0 && s2.length() > 0) return 1;
		if (s2.length() == 0 && s1.length() > 0) return -1;
		if (s2.length() == 0 && s1.length() == 0) return 0;

		char firstChar = s1.charAt(s1.length() - 1);
		char secondChar = s2.charAt(s2.length() - 1);

		if (this.ignoreCharacters.contains(firstChar))
			return (secondChar - firstChar) * -1;
		else if (this.ignoreCharacters.contains(secondChar))
			return (firstChar - secondChar) * -1;
		else
			return firstChar - secondChar;
	}

	public void removeIgnoredCharacter(char c) {
		this.ignoreCharacters.remove(c);
	}

}