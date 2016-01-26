package net.netcoding.niftycore.minecraft.scheduler;

import net.netcoding.niftycore.NiftyCore;
import net.netcoding.niftycore.reflection.Reflection;
import net.netcoding.niftycore.util.StringUtil;

import java.util.concurrent.TimeUnit;

public class MinecraftScheduler {

	private static final Object SCHEDULER_OBJ;
	private static final Reflection BUNGEE_TASK = new Reflection("ScheduledTask", "net.md_5.bungee.api.scheduler");
	private static final Reflection BUKKIT_TASK = new Reflection("BukkitTask", "org.bukkit.scheduler");
	static final Reflection SCHEDULER = new Reflection(StringUtil.format("{0}Scheduler", (NiftyCore.isBungee() ? "Task" : "Bukkit")), (NiftyCore.isBungee() ? BUNGEE_TASK.getPackagePath() : BUKKIT_TASK.getPackagePath()));

	private MinecraftScheduler() { }

	static {
		try {
			Reflection server = new Reflection((NiftyCore.isBungee() ? "ProxyServer" : "Bukkit"), (NiftyCore.isBungee() ? "net.md_5.bungee.api" : "org.bukkit"));
			Object serverObj = NiftyCore.isBungee() ? server.invokeMethod("getInstance", null) : null;
			SCHEDULER_OBJ = server.invokeMethod("getScheduler", serverObj);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void cancel(int id) {
		try {
			SCHEDULER.invokeMethod(StringUtil.format("cancel{0}", (NiftyCore.isBungee() ? "" : "Task")), SCHEDULER_OBJ, id);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void cancel(Object plugin) {
		try {
			SCHEDULER.invokeMethod(StringUtil.format("cancel{0}", (NiftyCore.isBungee() ? "" : "Tasks")), SCHEDULER_OBJ, plugin);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void cancel(ScheduledTask<?> task) throws Exception {
		cancel(task.getId());
	}

	public static ScheduledTask<?> repeat(Runnable task) {
		try {
			return repeat(NiftyCore.getPlugin(), task);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static <T> ScheduledTask<T> repeat(final T plugin, final Runnable task) {
		try {
			Runnable repeater = new Runnable() {
				@Override
				public void run() {
					task.run();
					repeat(plugin, task);
				}
			};

			return schedule(plugin, repeater);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static ScheduledTask<?> repeatAsync(Runnable task) {
		try {
			return repeatAsync(NiftyCore.getPlugin(), task);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static <T> ScheduledTask<T> repeatAsync(final T plugin, final Runnable task) {
		try {
			Runnable repeater = new Runnable() {
				@Override
				public void run() {
					task.run();
					repeatAsync(plugin, task);
				}
			};

			return runAsync(plugin, repeater);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static ScheduledTask<?> runAsync(Runnable task) {
		try {
			return runAsync(NiftyCore.getPlugin(), task);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static ScheduledTask<?> runAsync(Runnable task, long delay) {
		try {
			return runAsync(NiftyCore.getPlugin(), task, delay);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static ScheduledTask<?> runAsync(Runnable task, long delay, long period) {
		try {
			return runAsync(NiftyCore.getPlugin(), task, delay, period);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static <T> ScheduledTask<T> runAsync(T plugin, Runnable task) {
		try {
			if (NiftyCore.isBungee()) {
				Object taskObj = SCHEDULER.invokeMethod("runAsync", SCHEDULER_OBJ, plugin, task);
				int taskId = (int)BUNGEE_TASK.invokeMethod("getId", taskObj);
				return new ScheduledTask<>(plugin, taskId, false);
			}

			Object taskObj = SCHEDULER.invokeMethod("runTaskAsynchronously", SCHEDULER_OBJ, plugin, task);
			int taskId = (int)BUKKIT_TASK.invokeMethod("getTaskId", taskObj);
			return new ScheduledTask<>(plugin, taskId, false);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static <T> ScheduledTask<T> runAsync(T plugin, final Runnable task, long delay) {
		try {
			if (NiftyCore.isBungee()) {
				return schedule(plugin, new Runnable() {
					@Override
					public void run() {
						runAsync(task);
					}
				}, delay);
			}

			Object taskObj = SCHEDULER.invokeMethod("runTaskLaterAsynchronously", SCHEDULER_OBJ, plugin, task, (delay < 0L ? 0L : delay));
			int taskId = (int)BUKKIT_TASK.invokeMethod("getTaskId", taskObj);
			return new ScheduledTask<>(plugin, taskId, false);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static <T> ScheduledTask<T> runAsync(T plugin, final Runnable task, long delay, long period) {
		try {
			if (NiftyCore.isBungee()) {
				return schedule(plugin, new Runnable() {
					@Override
					public void run() {
						runAsync(task);
					}
				}, delay, period);
			}

			Object taskObj = SCHEDULER.invokeMethod("runTaskTimerAsynchronously", SCHEDULER_OBJ, plugin, task, (delay < 0L ? 0L : delay), (period < 0L ? 0L : period));
			int taskId = (int)BUKKIT_TASK.invokeMethod("getTaskId", taskObj);
			return new ScheduledTask<>(plugin, taskId, false);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static ScheduledTask<?> schedule(Runnable task) {
		try {
			return schedule(NiftyCore.getPlugin(), task);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static <T> ScheduledTask<T> schedule(T plugin, Runnable task) {
		try {
			if (NiftyCore.isBungee())
				return schedule(plugin, task, 0L);

			int taskId = (int)SCHEDULER.invokeMethod("scheduleSyncDelayedTask", SCHEDULER_OBJ, plugin, task);
			return new ScheduledTask<>(plugin, taskId, true);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static ScheduledTask<?> schedule(Runnable task, long delay) {
		try {
			return schedule(NiftyCore.getPlugin(), task, delay);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static ScheduledTask<?> schedule(Runnable task, long delay, long period) {
		try {
			return schedule(NiftyCore.getPlugin(), task, delay, period);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static <T> ScheduledTask<T> schedule(T plugin, Runnable task, long delay) {
		try {
			if (NiftyCore.isBungee()) {
				Object taskObj = SCHEDULER.invokeMethod("schedule", SCHEDULER_OBJ, plugin, task, (delay < 0L ? 0L : delay), TimeUnit.MILLISECONDS);
				int taskId = (int)BUNGEE_TASK.invokeMethod("getId", taskObj);
				return new ScheduledTask<>(plugin, taskId, true);
			}

			int taskId = (int)SCHEDULER.invokeMethod("scheduleSyncDelayedTask", SCHEDULER_OBJ, plugin, task, (delay < 0L ? 0L : delay));
			return new ScheduledTask<>(plugin, taskId, true);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static <T> ScheduledTask<T> schedule(T plugin, Runnable task, long delay, long period) {
		try {
			if (NiftyCore.isBungee()) {
				Object taskObj = SCHEDULER.invokeMethod("schedule", SCHEDULER_OBJ, plugin, task, (delay < 0L ? 0L : delay), (period < 0L ? 0L : period), TimeUnit.MILLISECONDS);
				int taskId = (int)BUNGEE_TASK.invokeMethod("getId", taskObj);
				return new ScheduledTask<>(plugin, taskId, true);
			}

			int taskId = (int)SCHEDULER.invokeMethod("scheduleSyncRepeatingTask", SCHEDULER_OBJ, plugin, task, (delay < 0L ? 0L : delay), (period < 0L ? 0L : period));
			return new ScheduledTask<>(plugin, taskId, true);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}