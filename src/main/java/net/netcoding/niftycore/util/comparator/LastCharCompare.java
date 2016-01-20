package net.netcoding.niftycore.util.comparator;

import java.util.Comparator;

public class LastCharCompare implements Comparator<String> {

	@Override
	public int compare(String s1, String s2) {
		if (s1.length() == 0 && s2.length() > 0) return 1;
		if (s2.length() == 0 && s1.length() > 0) return -1;
		if (s2.length() == 0 && s1.length() == 0) return 0;

		return s1.charAt(s1.length() - 1) - s2.charAt(s2.length() - 1);
	}

}