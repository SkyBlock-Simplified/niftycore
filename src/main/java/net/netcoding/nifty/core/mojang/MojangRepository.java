package net.netcoding.nifty.core.mojang;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.netcoding.nifty.core.NiftyCore;
import net.netcoding.nifty.core.api.scheduler.MinecraftScheduler;
import net.netcoding.nifty.core.http.*;
import net.netcoding.nifty.core.http.exceptions.HttpConnectionException;
import net.netcoding.nifty.core.mojang.exceptions.ProfileNotFoundException;
import net.netcoding.nifty.core.util.ListUtil;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.util.concurrent.Concurrent;
import net.netcoding.nifty.core.util.concurrent.ConcurrentList;
import net.netcoding.nifty.core.util.concurrent.ConcurrentSet;
import net.netcoding.nifty.core.util.misc.Callback;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * A collection of methods to query UniqueIDs and Names.
 */
public abstract class MojangRepository<T extends MojangProfile, P> {

	// API: http://wiki.vg/Mojang_API
	protected static final transient Gson GSON = new Gson();
	protected static final int PROFILES_PER_REQUEST = 100;
	protected static long LAST_HTTP_REQUEST = System.currentTimeMillis();
	protected static boolean API_AVAILABLE = true;
	protected final transient ConcurrentSet<T> cache = Concurrent.newSet();

	static {
		MinecraftScheduler.getInstance().runAsync(() -> {
			boolean available = false;

			try {
				HttpResponse response = HttpClient.get(Services.SERVICE_MOJANG_STATUS);
				JsonArray services = new JsonParser().parse(response.getBody().toString()).getAsJsonArray();

				for (int i = 0; i < services.size(); i++) {
					JsonObject status = services.get(i).getAsJsonObject();

					if (status.get(Services.SERVICE_MOJANG_STATUS.getHost()) != null)
						available = !"red".equals(status.get(Services.SERVICE_MOJANG_STATUS.getHost()).getAsString());
				}
			} catch (Exception ignore) { }

			API_AVAILABLE = available;
		}, 0, 5 * (NiftyCore.isBungee() ? 60000 : 1200));
	}

	@SuppressWarnings("unchecked")
	protected final Class<T> getSuperClass() {
		ParameterizedType superClass = (ParameterizedType)this.getClass().getGenericSuperclass();
		return (Class<T>)(superClass.getActualTypeArguments().length == 0 ? BasicMojangProfile.class : superClass.getActualTypeArguments()[0]);
	}

	@SuppressWarnings("unchecked")
	protected final Class<T[]> getSuperClassArray() {
		return (Class<T[]>)Array.newInstance(this.getSuperClass(), 0).getClass();
	}

	protected abstract boolean isOnline();

	protected abstract void processOfflineUsernames(List<T> profiles, ConcurrentList<String> userList);

	protected abstract void processOnlineUsernames(List<T> profiles, ConcurrentList<String> userList);

	protected abstract T processOfflineUniqueId(UUID uniqueId);

	protected abstract T processOnlineUniqueId(UUID uniqueId);

	/**
	 * Locates the profile associated with the given player.
	 *
	 * @param player Player to search with.
	 * @return Profile associated with the given player.
	 * @throws ProfileNotFoundException If unable to locate users profile.
	 */
	public abstract T searchByPlayer(P player) throws ProfileNotFoundException;

	/**
	 * Locates the profile associated with the given player.
	 *
	 * @param player Player to search with.
	 * @param callback Callback to handle the result or error with.
	 */
	public final void searchByPlayer(P player, Callback<T> callback) {
		Preconditions.checkArgument(callback != null, "Callback cannot be NULL!");

		MinecraftScheduler.getInstance().runAsync(() -> {
			T profile = null;
			Throwable throwable = null;

			try {
				profile = this.searchByPlayer(player);
			} catch (ProfileNotFoundException pnfex) {
				throwable = pnfex;
			}

			callback.handle(profile, throwable);
		});
	}

	/**
	 * Locates the profiles associated with the given players.
	 *
	 * @param players Players to search with.
	 * @return Profiles associated with the given players.
	 * @throws ProfileNotFoundException If unable to locate any users profile.
	 */
	public abstract T[] searchByPlayer(P[] players) throws ProfileNotFoundException;

	/**
	 * Locates the profiles associated with the given players.
	 *
	 * @param players Players to search with.
	 * @param callback Callback to handle the result or error with.
	 */
	public final void searchByPlayer(P[] players, Callback<T[]> callback) {
		Preconditions.checkArgument(callback != null, "Callback cannot be NULL!");

		MinecraftScheduler.getInstance().runAsync(() -> {
			T[] profiles = null;
			Throwable throwable = null;

			try {
				profiles = this.searchByPlayer(players);
			} catch (ProfileNotFoundException pnfex) {
				throwable = pnfex;
			}

			callback.handle(profiles, throwable);
		});
	}

	/**
	 * Locates the profiles associated with the given players.
	 *
	 * @param players Players to search with.
	 * @return Profiles associated with the given players.
	 * @throws ProfileNotFoundException If unable to locate any users profile.
	 */
	public abstract T[] searchByPlayer(Collection<? extends P> players) throws ProfileNotFoundException;

	/**
	 * Locates the profiles associated with the given players.
	 *
	 * @param players Players to search with.
	 * @param callback Callback to handle the result or error with.
	 */
	public final void searchByPlayer(Collection<? extends P> players, Callback<T[]> callback) {
		Preconditions.checkArgument(callback != null, "Callback cannot be NULL!");

		MinecraftScheduler.getInstance().runAsync(() -> {
			T[] profiles = null;
			Throwable throwable = null;

			try {
				profiles = this.searchByPlayer(players);
			} catch (ProfileNotFoundException pnfex) {
				throwable = pnfex;
			}

			callback.handle(profiles, throwable);
		});
	}

	/**
	 * Locates the profile associated with the given username.
	 *
	 * @param username Username to search with.
	 * @return Profile associated with the given username.
	 * @throws ProfileNotFoundException If unable to locate users profile.
	 */
	public final T searchByUsername(String username) throws ProfileNotFoundException {
		try {
			return this.searchByUsername(Collections.singletonList(username))[0];
		} catch (ProfileNotFoundException pnfex) {
			throw new ProfileNotFoundException(pnfex.getReason(), ProfileNotFoundException.LookupType.USERNAME, pnfex.getCause(), username);
		}
	}

	/**
	 * Locates the profile associated with the given username.
	 *
	 * @param username Username to search with.
	 * @param callback Callback to handle the result or error with.
	 */
	public final void searchByUsername(String username, Callback<T> callback) {
		Preconditions.checkArgument(callback != null, "Callback cannot be NULL!");

		MinecraftScheduler.getInstance().runAsync(() -> {
			T profile = null;
			Throwable throwable = null;

			try {
				profile = this.searchByUsername(username);
			} catch (ProfileNotFoundException pnfex) {
				throwable = pnfex;
			}

			callback.handle(profile, throwable);
		});
	}

	/**
	 * Locates the profiles associated with the given usernames.
	 *
	 * @param usernames Usernames to search with.
	 * @return Profiles associated with the given usernames.
	 * @throws ProfileNotFoundException If unable to locate any users profile.
	 */
	public final T[] searchByUsername(String[] usernames) throws ProfileNotFoundException {
		return this.searchByUsername(Arrays.asList(usernames));
	}

	/**
	 * Locates the profiles associated with the given usernames.
	 *
	 * @param usernames Usernames to search with.
	 * @param callback Callback to handle the result or error with.
	 */
	public final void searchByUsername(String[] usernames, Callback<T[]> callback) {
		Preconditions.checkArgument(callback != null, "Callback cannot be NULL!");

		MinecraftScheduler.getInstance().runAsync(() -> {
			T[] profiles = null;
			Throwable throwable = null;

			try {
				profiles = this.searchByUsername(usernames);
			} catch (ProfileNotFoundException pnfex) {
				throwable = pnfex;
			}

			callback.handle(profiles, throwable);
		});
	}

	/**
	 * Locates the profiles associated with the given usernames.
	 *
	 * @param usernames Usernames to search with.
	 * @return Profiles associated with the given usernames.
	 * @throws ProfileNotFoundException If unable to locate any users profile.
	 */
	public final T[] searchByUsername(Collection<String> usernames) throws ProfileNotFoundException {
		final ProfileNotFoundException.LookupType type = ProfileNotFoundException.LookupType.USERNAMES;
		ConcurrentList<T> profiles = Concurrent.newList();
		HttpStatus status = HttpStatus.OK;

		try {
			ConcurrentList<String> userList = Concurrent.newList(usernames);

			// Check Online Servers
			this.processOnlineUsernames(profiles, userList);

			// Remove Expired Cache Profiles
			this.cache.stream().filter(T::hasExpired).forEach(this.cache::remove);

			// Check Cache Profiles
			if (!this.cache.isEmpty()) {
				for (String name : userList) {
					String criteriaName = name.toLowerCase();

					for (T profile : this.cache) {
						if (profile.getName().equalsIgnoreCase(criteriaName)) {
							profiles.add(profile);
							userList.remove(name);
							break;
						}
					}

					for (T profile : this.cache) {
						if (profile.getName().toLowerCase().startsWith(criteriaName)) {
							profiles.add(profile);
							userList.remove(name);
							break;
						}
					}
				}
			}

			// Check Offline Player Cache
			this.processOfflineUsernames(profiles, userList);

			// Query Mojang API
			if (!userList.isEmpty() && API_AVAILABLE) {
				HttpHeader contentType = new HttpHeader("Content-Type", "application/json");
				String[] userArray = ListUtil.toArray(userList, String.class);
				int start = 0;
				int i = 0;

				do {
					int end = PROFILES_PER_REQUEST * (i + 1);
					if (end > userList.size()) end = userList.size();
					String[] batch = Arrays.copyOfRange(userArray, start, end);
					HttpBody body = new HttpBody(GSON.toJson(batch));
					long wait = LAST_HTTP_REQUEST + 100 - System.currentTimeMillis();

					try {
						if (wait > 0) Thread.sleep(wait);
						HttpResponse response = HttpClient.post(Services.API_NAME_TO_UUID, body, contentType);

						if (HttpStatus.NO_CONTENT != response.getStatus()) {
							T[] result = GSON.fromJson(response.getBody().toString(), this.getSuperClassArray());

							if (result != null && result.length > 0) {
								profiles.addAll(Arrays.asList(result));
								this.cache.addAll(Arrays.asList(result));
							}
						}
					} catch (HttpConnectionException hcex) {
						if (HttpStatus.TOO_MANY_REQUESTS == (status = hcex.getStatus()))
							break;

						throw hcex;
					} finally {
						LAST_HTTP_REQUEST = System.currentTimeMillis();
						start = end;
						i++;
					}
				} while (start < userList.size());

				for (T profile : profiles)
					userList.remove(profile.getName());

				for (String user : userList) {
					long wait = LAST_HTTP_REQUEST + 100 - System.currentTimeMillis();

					try {
						if (wait > 0) Thread.sleep(wait);
						HttpResponse response = HttpClient.get(Services.getNameUrl(user));

						if (HttpStatus.NO_CONTENT != response.getStatus()) {
							T result = GSON.fromJson(response.getBody().toString(), this.getSuperClass());

							if (result != null) {
								profiles.add(result);
								this.cache.add(result);
							}
						}
					} catch (HttpConnectionException hcex) {
						if (HttpStatus.TOO_MANY_REQUESTS == (status = hcex.getStatus()))
							break;

						throw hcex;
					} finally {
						LAST_HTTP_REQUEST = System.currentTimeMillis();
					}
				}

				for (T profile : profiles)
					userList.remove(profile.getName());

				for (String user : userList) {
					long wait = LAST_HTTP_REQUEST + 100 - System.currentTimeMillis();

					try {
						if (wait > 0) Thread.sleep(wait);
						HttpResponse response = HttpClient.get(Services.getNameUrl(user, false));

						if (HttpStatus.NO_CONTENT != response.getStatus()) {
							T result = GSON.fromJson(response.getBody().toString(), this.getSuperClass());

							if (result != null) {
								profiles.add(result);
								this.cache.add(result);
							}
						}
					} catch (HttpConnectionException hcex) {
						if (HttpStatus.TOO_MANY_REQUESTS == (status = hcex.getStatus()))
							break;

						throw hcex;
					} finally {
						LAST_HTTP_REQUEST = System.currentTimeMillis();
					}
				}
			}
		} catch (Exception ex) {
			throw new ProfileNotFoundException(ProfileNotFoundException.Reason.EXCEPTION, type, ex, ListUtil.toArray(usernames, String.class));
		}

		if (profiles.isEmpty()) {
			ProfileNotFoundException.Reason reason = ProfileNotFoundException.Reason.NO_PREMIUM_PLAYER;

			if (status == HttpStatus.TOO_MANY_REQUESTS)
				reason = ProfileNotFoundException.Reason.RATE_LIMITED;

			throw new ProfileNotFoundException(reason, type, ListUtil.toArray(usernames, String.class));
		}

		return ListUtil.toArray(profiles, this.getSuperClass());
	}

	/**
	 * Locates the profiles associated with the given usernames.
	 *
	 * @param usernames Usernames to search with.
	 * @param callback Callback to handle the result or error with.
	 */
	public final void searchByUsername(Collection<String> usernames, Callback<T[]> callback) {
		Preconditions.checkArgument(callback != null, "Callback cannot be NULL!");

		MinecraftScheduler.getInstance().runAsync(() -> {
			T[] profiles = null;
			Throwable throwable = null;

			try {
				profiles = this.searchByUsername(usernames);
			} catch (ProfileNotFoundException pnfex) {
				throwable = pnfex;
			}

			callback.handle(profiles, throwable);
		});
	}

	/**
	 * Locates the profile associated with the given Unique ID.
	 *
	 * @param uniqueId Unique ID to search with.
	 * @return Profile associated with the given Unique ID.
	 * @throws ProfileNotFoundException If unable to locate users profile.
	 */
	public final T searchByUniqueId(UUID uniqueId) throws ProfileNotFoundException {
		final ProfileNotFoundException.LookupType type = ProfileNotFoundException.LookupType.UNIQUE_ID;
		T found;
		HttpStatus status = HttpStatus.OK;

		try {
			// Check Online Servers
			found = this.processOnlineUniqueId(uniqueId);

			// Remove Expired Cache Profiles
			this.cache.stream().filter(MojangProfile::hasExpired).forEach(this.cache::remove);

			// Check Cache Profiles
			if (found == null) {
				for (T profile : this.cache) {
					if (profile.getUniqueId().equals(uniqueId)) {
						found = profile;
						break;
					}
				}
			}

			// Check Offline Player Cache
			if (found == null)
				found = this.processOfflineUniqueId(uniqueId);

			if (found == null && API_AVAILABLE) {
				try {
					long wait = LAST_HTTP_REQUEST + 100 - System.currentTimeMillis();
					if (wait > 0) Thread.sleep(wait);
					HttpResponse response = HttpClient.get(Services.getNameHistoryUrl(uniqueId));

					if (HttpStatus.NO_CONTENT != response.getStatus()) {
						UUIDSearchResult[] results = GSON.fromJson(response.getBody().toString(), UUIDSearchResult[].class);

						if (results != null && results.length > 0) {
							UUIDSearchResult result = results[results.length - 1];
							JsonObject json = new JsonObject();
							json.addProperty("id", uniqueId.toString());
							json.addProperty("name", result.getName());
							found = GSON.fromJson(json.toString(), this.getSuperClass());
							this.cache.add(found);
						}
					}
				} catch (HttpConnectionException hcex) {
					if (HttpStatus.TOO_MANY_REQUESTS != (status = hcex.getStatus()))
						throw hcex;
				} finally {
					LAST_HTTP_REQUEST = System.currentTimeMillis();
				}
			}
		} catch (Exception ex) {
			throw new ProfileNotFoundException(ProfileNotFoundException.Reason.EXCEPTION, type, ex, uniqueId);
		}

		if (found == null) {
			ProfileNotFoundException.Reason reason = ProfileNotFoundException.Reason.NO_PREMIUM_PLAYER;

			if (status == HttpStatus.TOO_MANY_REQUESTS)
				reason = ProfileNotFoundException.Reason.RATE_LIMITED;

			throw new ProfileNotFoundException(reason, type, uniqueId);
		}

		return found;
	}

	/**
	 * Locates the profile associated with the given Unique ID.
	 *
	 * @param uniqueId Unique ID to search with.
	 * @param callback The callback to handle the result or error with.
	 */
	public final void searchByUniqueId(UUID uniqueId, Callback<T> callback) {
		Preconditions.checkArgument(callback != null, "Callback cannot be NULL!");

		MinecraftScheduler.getInstance().runAsync(() -> {
			T profile = null;
			Throwable throwable = null;

			try {
				profile = this.searchByUniqueId(uniqueId);
			} catch (ProfileNotFoundException pnfex) {
				throwable = pnfex;
			}

			callback.handle(profile, throwable);
		});
	}

	protected static class UUIDSearchResult {

		private String name;
		private long changedToAt;

		public long getChangedToAt() {
			return this.changedToAt;
		}

		public String getName() {
			return this.name;
		}

	}

	/**
	 * Mojang Service and API URLs
	 */
	public static final class Services {

		public static final URL SERVICE_MINECRAFT = getUrl("minecraft.net");
		public static final URL SERVICE_MOJANG_ACCOUNT = getUrl("account.mojang.com");
		public static final URL SERVICE_MOJANG_API = getUrl("api.mojang.com");
		public static final URL SERVICE_MOJANG_AUTH = getUrl("auth.mojang.com");
		public static final URL SERVICE_MOJANG_AUTHSERVER = getUrl("authserver.mojang.com");
		public static final URL SERVICE_MOJANG_SESSION = getUrl("sessionserver.mojang.com");
		public static final URL SERVICE_MOJANG_STATUS = getUrl("sessionserver.mojang.com");
		public static final URL SERVICE_MINECRAFT_SESSION = getUrl("session.minecraft.net");
		public static final URL SERVICE_MINECRAFT_SKINS = getUrl("skins.minecraft.net");
		public static final URL SERVICE_MINECRAFT_TEXTURES = getUrl("textures.minecraft.net");

		public static final URL API_UUID_TO_NAME = getUrl(StringUtil.format("{0}/users/profiles/minecraft", SERVICE_MOJANG_API.getHost()));
		public static final URL API_NAME_TO_UUID = getUrl(StringUtil.format("{0}/profiles/minecraft", SERVICE_MOJANG_API.getHost()));
		public static final URL SESSIONSERVER_SKIN_CAPE = getUrl(StringUtil.format("{0}/session/minecraft/profile", SERVICE_MOJANG_SESSION.getHost()));
		public static final URL AUTHSERVER_AUTHENTICATE = getUrl(StringUtil.format("{0}/authenticate", SERVICE_MOJANG_AUTHSERVER.getHost()));
		public static final URL AUTHSERVER_REFRESH = getUrl(StringUtil.format("{0}/refresh", SERVICE_MOJANG_AUTHSERVER.getHost()));
		public static final URL AUTHSERVER_VALIDATE = getUrl(StringUtil.format("{0}/validate", SERVICE_MOJANG_AUTHSERVER.getHost()));
		public static final URL AUTHSERVER_SIGNOUT = getUrl(StringUtil.format("{0}/signout", SERVICE_MOJANG_AUTHSERVER.getHost()));
		public static final URL AUTHSERVER_INVALIDATE = getUrl(StringUtil.format("{0}/invalidate", SERVICE_MOJANG_AUTHSERVER.getHost()));

		public static URL getNameUrl(String username) {
			return getNameUrl(username, true);
		}

		public static URL getNameUrl(String username, boolean useAt) {
			return getUrl(StringUtil.format("{0}/{1}{2}", API_UUID_TO_NAME.toString(), username, (useAt ? "?at=0" : "")));
		}

		public static URL getNameHistoryUrl(UUID uniqueId) {
			return getUrl(StringUtil.format("{0}/user/profiles/{1}/names", SERVICE_MOJANG_API.toString(), uniqueId.toString().replace("-", "")));
		}

		public static URL getPropertiesUrl(UUID uniqueId) {
			return getPropertiesUrl(uniqueId, true);
		}

		public static URL getPropertiesUrl(UUID uniqueId, boolean unsigned) {
			return getUrl(StringUtil.format("{0}/{1}?unsigned={2}", SESSIONSERVER_SKIN_CAPE.toString(), uniqueId.toString().replace("-", ""), String.valueOf(unsigned)));
		}

		private static URL getUrl(String host) {
			if (!host.startsWith("https://"))
				host = StringUtil.format("https://{0}", host);

			try {
				return new URL(host);
			} catch (MalformedURLException muex) {
				throw new IllegalArgumentException(StringUtil.format("Unable to create URL {0}!", host));
			}
		}

	}

}