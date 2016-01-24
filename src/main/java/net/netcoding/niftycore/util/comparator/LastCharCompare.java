package net.netcoding.niftycore.util.comparator;

import net.netcoding.niftycore.util.concurrent.ConcurrentSet;

import java.util.Comparator;

public class LastCharCompare implements Comparator<String> {

	private final ConcurrentSet<Character> ignoreCharacters = new ConcurrentSet<>();

	public void addIgnoreCharacter(char c) {
		this.ignoreCharacters.add(c);
	}

	@Override
	public int compare(String s1, String s2) {
		if (s1.length() == 0 && s2.length() > 0) return 1;
		if (s2.length() == 0 && s1.length() > 0) return -1;
		if (s2.length() == 0 && s1.length() == 0) return 0;

		char firstChar = s1.charAt(s1.length() - 1);
		boolean isIgnored = this.ignoreCharacters.contains(firstChar);
		return firstChar - s2.charAt(s2.length() - 1) * (isIgnored ? -1 : 0);
	}

	public void removeIgnoredCharacter(char c) {
		this.ignoreCharacters.remove(c);
	}

}