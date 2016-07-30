package net.netcoding.nifty.core.mojang;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.netcoding.nifty.core.api.MinecraftServer;
import net.netcoding.nifty.core.api.scheduler.MinecraftScheduler;
import net.netcoding.nifty.core.http.HttpClient;
import net.netcoding.nifty.core.http.HttpResponse;
import net.netcoding.nifty.core.http.HttpStatus;
import net.netcoding.nifty.core.http.exceptions.HttpConnectionException;
import net.netcoding.nifty.core.mojang.exceptions.ProfileNotFoundException;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.util.misc.Callback;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static net.netcoding.nifty.core.mojang.MojangRepository.GSON;
import static net.netcoding.nifty.core.mojang.MojangRepository.LAST_HTTP_REQUEST;

/**
 * Container for a players unique id and name.
 */
public abstract class MojangProfile<T extends MojangProfile<T>> implements OnlineProfile {

	private static final Pattern UUID_FIX = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
	private String id = "";
	private UUID uuid;
	protected String name;
	private String ip = "";
	private int port = 0;
	private InetSocketAddress ipAddress;
	private boolean legacy = false;
	private boolean demo = false;
	private URL skinUrl;
	private URL capeUrl;
	private long updated = System.currentTimeMillis();
	private long lastHttpRequest = System.currentTimeMillis();
	// http://skins.minecraft.net/MinecraftSkins/<username>.png

	@Override
	public final boolean equals(Object obj) {
		return obj == this || obj instanceof Profile && this.getUniqueId().equals(((Profile)obj).getUniqueId());
	}

	@Override
	public final InetSocketAddress getAddress() {
		if (StringUtil.notEmpty(this.ip) && this.ipAddress == null && this.isOnline())
			this.ipAddress = new InetSocketAddress(this.ip, this.port);

		return this.ipAddress;
	}

	/**
	 * Gets the cape associated with this profile.
	 *
	 * @return Cape url, null if profile has no cape or has not yet been requested.
	 * @see #loadProperties(Callback)
	 */
	public final URL getCapeUrl() {
		return this.capeUrl;
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the server this profile belongs to.
	 *
	 * @return BungeeServer Server object.
	 */
	public abstract MinecraftServer<? extends MojangProfile> getServer();

	/**
	 * Gets the skin associated with this profile.
	 *
	 * @return Skin url, null if profile has no skin or has not yet been requested.
	 * @see #loadProperties(Callback)
	 */
	public final URL getSkinUrl() {
		return this.skinUrl;
	}

	/**
	 * Gets the players UUID.
	 *
	 * @return Player UUID.
	 */
	@Override
	public final UUID getUniqueId() {
		if (this.uuid == null)
			this.uuid = UUID.fromString(UUID_FIX.matcher(this.id.replace("-", "")).replaceAll("$1-$2-$3-$4-$5"));

		return this.uuid;
	}

	/**
	 * Checks if this profile has an assigned ip address.
	 *
	 * @return True if address exists, otherwise false.
	 */
	public final boolean hasAddress() {
		return this.getAddress() != null;
	}

	/**
	 * Checks if this players profile is expired.
	 *
	 * @return True if expired, otherwise false.
	 */
	public final boolean hasExpired() {
		return System.currentTimeMillis() - this.updated >= 1800000;
	}

	@Override
	public final int hashCode() {
		return this.getUniqueId().hashCode();
	}

	/**
	 * Gets if the account is unpaid.
	 *
	 * @return True if unpaid, otherwise false.
	 */
	public final boolean isDemo() {
		return this.demo;
	}

	/**
	 * Gets if the account has not been migrated to Mojang.
	 *
	 * @return True if not migrated, otherwise false.
	 */
	public final boolean isLegacy() {
		return this.legacy;
	}

	/**
	 * Checks if this profile is online anywhere on BungeeCord.
	 *
	 * @return True if online, otherwise false.
	 */
	@Override
	public abstract boolean isOnline();

	/**
	 * Loads this profiles skin and cape.
	 * <p>
	 * This request is asynchronous, it requires a callback if you wish to act upon the response.
	 *
	 * @param callback A call back used for when the request is finished.
	 */
	@SuppressWarnings("unchecked")
	public final void loadProperties(Callback<T, ProfileNotFoundException> callback) {
		MinecraftScheduler.getInstance().runAsync(() -> {
			final ProfileNotFoundException.LookupType type = ProfileNotFoundException.LookupType.UNIQUE_ID;
			HttpStatus status = HttpStatus.OK;
			ProfileNotFoundException pnfex = null;

			try {
				if (MojangRepository.API_AVAILABLE) {
					try {
						long wait = this.lastHttpRequest + 100 - System.currentTimeMillis();
						if (wait > 0) Thread.sleep(wait);
						HttpResponse response = HttpClient.get(MojangRepository.Services.getPropertiesUrl(this.getUniqueId()));

						if (HttpStatus.NO_CONTENT != response.getStatus()) {
							ProfileSearchResult result = GSON.fromJson(response.getBody().toString(), ProfileSearchResult.class);

							if (result != null) {
								JsonObject textures = result.getProperties().getValue().getAsJsonObject("textures");

								if (textures.has("SKIN"))
									this.skinUrl = new URL(textures.getAsJsonObject("SKIN").get("url").getAsString());

								if (textures.has("CAPE"))
									this.capeUrl = new URL(textures.getAsJsonObject("CAPE").get("url").getAsString());
							}
						}
					} catch (HttpConnectionException hcex) {
						if (HttpStatus.TOO_MANY_REQUESTS != hcex.getStatus())
							throw hcex;
					} finally {
						LAST_HTTP_REQUEST = System.currentTimeMillis();
					}
				}
			} catch (Exception ex) {
				pnfex = new ProfileNotFoundException(ProfileNotFoundException.Reason.EXCEPTION, type, ex, this.getUniqueId());
			}

			if (callback != null)
				callback.handle((T)this, pnfex);
		});
	}

	@Override
	public final String toString() {
		return StringUtil.format("'{'{0},{1}'}'", this.getUniqueId(), this.getName());
	}

	protected static class ProfileSearchResult {

		private String id;
		private String name;
		@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
		private List<PropertiesSearchResult> properties;

		public PropertiesSearchResult getProperties() {
			return this.properties.get(0);
		}

		protected static class PropertiesSearchResult {

			private String name;
			private String value;
			private String signature;

			public JsonObject getValue() {
				return new JsonParser().parse(new String(Base64Coder.decode(this.value), StandardCharsets.UTF_8)).getAsJsonObject();
			}

		}

	}


}