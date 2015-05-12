package net.netcoding.niftycore.minecraft;

public class MinecraftVersion {

	static final MinecraftVersion DEFAULT = new MinecraftVersion("", 0);
	private final String name;
	private final int protocol;

	public MinecraftVersion(String name, int protocol) {
		this.name = name;
		this.protocol = protocol;
	}

	public String getName() {
		return this.name;
	}

	public int getProtocol() {
		return this.protocol;
	}

}