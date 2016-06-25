package net.netcoding.nifty.core.mojang;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.netcoding.nifty.core.NiftyCore;
import net.netcoding.nifty.core.http.HttpClient;
import net.netcoding.nifty.core.http.HttpHeader;
import net.netcoding.nifty.core.http.HttpResponse;
import net.netcoding.nifty.core.mojang.exceptions.ProfileNotFoundException;
import net.netcoding.nifty.core.util.ListUtil;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.http.HttpBody;
import net.netcoding.nifty.core.http.HttpStatus;
import net.netcoding.nifty.core.http.exceptions.HttpConnectionException;
import net.netcoding.nifty.core.api.scheduler.MinecraftScheduler;
import net.netcoding.nifty.core.util.concurrent.ConcurrentList;
import net.netcoding.nifty.core.util.concurrent.ConcurrentSet;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unchecked")
/**
 * A collection of methods to locate player UUID and Name throughout Bungee or offline.
 */
public abstract class MojangRepository<T extends MojangProfile, P> {

	// API: http://wiki.vg/Mojang_API
	protected static final transient ConcurrentSet<MojangProfile> CACHE = new ConcurrentSet<>();
	protected static final transient Gson GSON = new Gson();
	protected static final String SERVICE_API = "api.mojang.com";
	protected static final int PROFILES_PER_REQUEST = 100;
	protected static long LAST_HTTP_REQUEST = System.currentTimeMillis();
	protected static boolean API_AVAILABLE = true;

	// Services
	/*public static final String SERVICE_MINECRAFT = "minecraft.net";
	public static final String SERVICE_SESSION_MINECRAFT = "session.minecraft.net";
	public static final String SERVICE_SKINS_MINECRAFT = "skins.minecraft.net";
	public static final String SERVICE_TEXTURES_MINECRAFT = "textures.minecraft.net";
	public static final String SERVICE_ACCOUNT_MOJANG = "account.mojang.com";
	public static final String SERVICE_AUTH_MOJANG = "auth.mojang.com";
	public static final String SERVICE_AUTHSERVER_MOJANG = "authserver.mojang.com";
	public static final String SERVICE_SESSION_MOJANG = "sessionserver.mojang.com";
	public static final String SERVICE_API_MOJANG = "api.mojang.com";*/

	// Method API URLs
	/*public static final String UUID_AT_TIME_MOJANG = "https://api.mojang.com/users/profiles/minecraft";
	public static final String STATUS_CHECK_MOJANG = "https://status.mojang.com/check";
	public static final String PLAYERNAME_UUID_MOJANG = "https://api.mojang.com/profiles/minecraft";
	public static final String UUID_PROFILE_SKIN_CAPE = "https://sessionserver.mojang.com/session/minecraft/profile";
	public static final String AUTH_AUTHENTICATE_MOJANG = "https://authserver.mojang.com/authenticate";
	public static final String AUTH_REFRESH_MOJANG = "https://authserver.mojang.com/refresh";
	public static final String AUTH_VALIDATE_MOJANG = "https://authserver.mojang.com/validate";
	public static final String AUTH_SIGNOUT_MOJANG = "https://authserver.mojang.com/signout";
	public static final String AUTH_INVALIDATE_MOJANG = "https://authserver.mojang.com/invalidate";*/

	static {
		MinecraftScheduler.getInstance().runAsync(() -> {
			boolean available = false;

			try {
				HttpResponse response = HttpClient.get(getStatusUrl());
				JsonArray services = new JsonParser().parse(response.getBody().toString()).getAsJsonArray();

				for (int i = 0; i < services.size(); i++) {
					JsonObject status = services.get(i).getAsJsonObject();

					if (status.get(SERVICE_API) != null)
						available = !"red".equals(status.get(SERVICE_API).getAsString());
				}
			} catch (Exception ignore) { }

			API_AVAILABLE = available;
		}, 0, 5 * (NiftyCore.isBungee() ? 60000 : 1200));
	}

	protected MojangRepository() { }

	protected static URL getProfilesUrl() throws MalformedURLException {
		return new URL("https://api.mojang.com/profiles/minecraft");
	}

	protected static URL getProfilesUrl(String username) throws MalformedURLException {
		return getProfilesUrl(username, true);
	}

	protected static URL getProfilesUrl(String username, boolean useAt) throws MalformedURLException {
		return new URL(StringUtil.format("https://api.mojang.com/users/profiles/minecraft/{0}{1}", username, (useAt ? "?at=0" : "")));
	}

	protected static URL getNamesUrl(UUID uniqueId) throws MalformedURLException {
		return new URL(StringUtil.format("https://api.mojang.com/user/profiles/{0}/names", uniqueId.toString().replace("-", "")));
	}

	protected static URL getStatusUrl() throws MalformedURLException {
		return new URL("https://status.mojang.com/check");
	}

	protected final Class<T> getSuperClass() {
		ParameterizedType superClass = (ParameterizedType)this.getClass().getGenericSuperclass();
		return (Class<T>)(superClass.getActualTypeArguments().length == 0 ? BasicMojangProfile.class : superClass.getActualTypeArguments()[0]);
	}

	protected final Class<T[]> getSuperClassArray() {
		return (Class<T[]>)Array.newInstance(this.getSuperClass(), 0).getClass();
	}

	protected abstract boolean isOnline();

	protected abstract void processOfflineUsernames(List<T> profiles, ConcurrentList<String> userList);

	protected abstract void processOnlineUsernames(List<T> profiles, ConcurrentList<String> userList);

	protected abstract T processOfflineUniqueId(UUID uniqueId);

	protected abstract T processOnlineUniqueId(UUID uniqueId);

	public abstract T searchByPlayer(P player) throws ProfileNotFoundException;

	public abstract T[] searchByPlayer(P[] player) throws ProfileNotFoundException;

	public abstract T[] searchByPlayer(Collection<? extends P> player) throws ProfileNotFoundException;

	/**
	 * Locates the profile associated with the given username.
	 *
	 * @param username Username to search with.
	 * @return Profile associated with the given username.
	 * @throws ProfileNotFoundException If unable to locate users profile.
	 */
	public T searchByUsername(String username) throws ProfileNotFoundException {
		try {
			return this.searchByUsername(Collections.singletonList(username))[0];
		} catch (ProfileNotFoundException pnfex) {
			throw new ProfileNotFoundException(pnfex.getReason(), ProfileNotFoundException.LookupType.USERNAME, pnfex.getCause(), username);
		}
	}

	/**
	 * Locates the profile associated with the given username.
	 *
	 * @param usernames Usernames to search with.
	 * @return Profiles associated with the given usernames.
	 * @throws ProfileNotFoundException If unable to locate any users profile.
	 */
	public T[] searchByUsername(String[] usernames) throws ProfileNotFoundException {
		return this.searchByUsername(Arrays.asList(usernames));
	}

	/**
	 * Locates the profiles associated with the given usernames.
	 *
	 * @param usernames Usernames to search with.
	 * @return Profiles associated with the given usernames.
	 * @throws ProfileNotFoundException If unable to locate any users profile.
	 */
	@SuppressWarnings("unchecked")
	public T[] searchByUsername(Collection<String> usernames) throws ProfileNotFoundException {
		final ProfileNotFoundException.LookupType type = ProfileNotFoundException.LookupType.USERNAMES;
		List<T> profiles = new ArrayList<>();
		HttpStatus status = HttpStatus.OK;

		try {
			ConcurrentList<String> userList = new ConcurrentList<>(usernames);

			// Check Online Servers
			this.processOnlineUsernames(profiles, userList);

			// Remove Expired Cache Profiles
			CACHE.stream().filter(MojangProfile::hasExpired).forEach(CACHE::remove);

			// Check Cache Profiles
			if (!CACHE.isEmpty()) {
				for (String name : userList) {
					String criteriaName = name.toLowerCase();

					for (MojangProfile profile : CACHE) {
						if (profile.getName().equalsIgnoreCase(criteriaName)) {
							profiles.add((T)profile);
							userList.remove(name);
							break;
						}
					}

					for (MojangProfile profile : CACHE) {
						if (profile.getName().toLowerCase().startsWith(criteriaName)) {
							profiles.add((T)profile);
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
						HttpResponse response = HttpClient.post(getProfilesUrl(), body, contentType);

						if (HttpStatus.NO_CONTENT != response.getStatus()) {
							T[] result = GSON.fromJson(response.getBody().toString(), this.getSuperClassArray());

							if (result != null && result.length > 0) {
								profiles.addAll(Arrays.asList(result));
								CACHE.addAll(Arrays.asList(result));
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
						HttpResponse response = HttpClient.get(getProfilesUrl(user));

						if (HttpStatus.NO_CONTENT != response.getStatus()) {
							T result = GSON.fromJson(response.getBody().toString(), this.getSuperClass());

							if (result != null) {
								profiles.add(result);
								CACHE.add(result);
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
						HttpResponse response = HttpClient.get(getProfilesUrl(user, false));

						if (HttpStatus.NO_CONTENT != response.getStatus()) {
							T result = GSON.fromJson(response.getBody().toString(), this.getSuperClass());

							if (result != null) {
								profiles.add(result);
								CACHE.add(result);
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
	 * Locates the profile associated with the given Unique ID.
	 *
	 * @param uniqueId Unique ID to search with.
	 * @return Profile associated with the given Unique ID.
	 * @throws ProfileNotFoundException If unable to locate users profile.
	 */
	@SuppressWarnings("unchecked")
	public T searchByUniqueId(final UUID uniqueId) throws ProfileNotFoundException {
		final ProfileNotFoundException.LookupType type = ProfileNotFoundException.LookupType.UNIQUE_ID;
		T found;
		HttpStatus status = HttpStatus.OK;

		try {
			// Check Online Servers
			found = this.processOnlineUniqueId(uniqueId);

			// Remove Expired Cache Profiles
			CACHE.stream().filter(MojangProfile::hasExpired).forEach(CACHE::remove);

			// Check Cache Profiles
			if (found == null) {
				for (MojangProfile profile : CACHE) {
					if (profile.getUniqueId().equals(uniqueId)) {
						found = (T)profile;
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
					HttpResponse response = HttpClient.get(getNamesUrl(uniqueId));

					if (HttpStatus.NO_CONTENT != response.getStatus()) {
						UUIDSearchResult[] results = GSON.fromJson(response.getBody().toString(), UUIDSearchResult[].class);

						if (results != null && results.length > 0) {
							UUIDSearchResult result = results[results.length - 1];
							JsonObject json = new JsonObject();
							json.addProperty("id", uniqueId.toString());
							json.addProperty("name", result.getName());
							found = GSON.fromJson(json.toString(), this.getSuperClass());
							CACHE.add(found);
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

	@SuppressWarnings("unused")
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

}