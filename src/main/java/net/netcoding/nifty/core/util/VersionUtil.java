package net.netcoding.nifty.core.util;

public class VersionUtil implements Comparable<VersionUtil> {

	private final String version;

	public VersionUtil(String version) {
		if (StringUtil.isEmpty(version))  throw new IllegalArgumentException("Version can not be null");
		if (!version.matches("[0-9]+(\\.[0-9]+)*")) throw new IllegalArgumentException("Invalid version format");
		this.version = version;
	}

	@Override
	public final int compareTo(VersionUtil that) {
		if (that == null) return 1;
		String[] thisParts = this.getVersion().split("\\.");
		String[] thatParts = that.getVersion().split("\\.");
		int length = Math.max(thisParts.length, thatParts.length);

		for (int i = 0; i < length; i++) {
			int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
			int thatPart = i < thatParts.length ?  Integer.parseInt(thatParts[i]) : 0;
			if (thisPart < thatPart) return -1;
			if (thisPart > thatPart) return 1;
		}

		return 0;
	}

	@Override
	public final boolean equals(Object obj) {
		return obj == this || obj instanceof VersionUtil && this.compareTo((VersionUtil) obj) == 0;
	}

	public final String getVersion() {
		return this.version;
	}

	@Override
	public final int hashCode() {
		return super.hashCode();
	}

	@Override
	public final String toString() {
		return this.getVersion();
	}

}