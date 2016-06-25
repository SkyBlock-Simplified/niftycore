package net.netcoding.nifty.core.api.ping;

import java.util.List;

class StatusResponse {

	private String description;
	private Players players;
	private Version version;
	private String favicon;

	public String getMotd() {
		return this.description;
	}

	public Players getPlayers() {
		return this.players;
	}

	public Version getVersion() {
		return this.version;
	}

	public String getFavicon() {
		return this.favicon;
	}

	class Players {

		private int max;
		private int online;
		private List<Player> sample;

		public int getMax() {
			return max;
		}

		public int getOnline() {
			return online;
		}

		public List<Player> getSample() {
			return sample;
		}

		class Player {

			private String name;
			private String id;

			public String getName() {
				return name;
			}

			public String getId() {
				return id;
			}

		}

	}

	class Version {

		private String name;
		private int protocol;

		public String getName() {
			return name;
		}

		public int getProtocol() {
			return protocol;
		}

	}

}