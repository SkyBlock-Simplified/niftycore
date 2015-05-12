package net.netcoding.niftycore.minecraft.scheduler;

import java.util.concurrent.TimeUnit;

import net.netcoding.niftycore.NiftyCore;
import net.netcoding.niftycore.reflection.Reflection;
import net.netcoding.niftycore.util.StringUtil;

public class MinecraftScheduler {

	private static final Object SCHEDULER_OBJ;
	private static final Reflection BUNGEE_TASK = new Reflection("ScheduledTask", "net.md_5.bungee.api.scheduler");
	private static final Reflection BUKKIT_TASK = new Reflection("BukkitTask", "org.bukkit.scheduler");
	static final Reflection SCHEDULER = new Reflection(StringUtil.format("{0}Scheduler", (NiftyCore.isBungee() ? "Task" : "Bukkit")), (NiftyCore.isBungee() ? BUNGEE_TASK.getPackagePath() : BUKKIT_TASK.getPackagePath()));

	static {
		try {
			Reflection server = new Reflection((NiftyCore.isBungee() ? "ProxyServer" : "Bukkit"), (NiftyCore.isBungee() ? "net.md_5.bungee.api" : "org.bukkit"));
			SCHEDULER_OBJ = server.invokeMethod("getScheduler", null);
			System.out.println(SCHEDULER_OBJ);
			System.out.println(SCHEDULER_OBJ.getClass());
			System.out.println(SCHEDULER_OBJ.getClass().getClass());
			//SCHEDULER = new Reflection(SCHEDULER_OBJ.getClass().getSimpleName(), SCHEDULER_OBJ.getClass().getPackage().toString());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static void cancel(int id) {
		try {
			SCHEDULER.invokeMethod(StringUtil.format("cancel{0}", (NiftyCore.isBungee() ? "" : "Task")), SCHEDULER_OBJ, id);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static <T extends net.md_5.bungee.api.plugin.Plugin> void cancel(T owner) {
		try {
			SCHEDULER.invokeMethod("cancel", SCHEDULER_OBJ, owner);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static <T extends org.bukkit.plugin.java.JavaPlugin> void cancel(T owner) {
		try {
			SCHEDULER.invokeMethod("cancelTasks", SCHEDULER_OBJ, owner);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static void cancel(ScheduledTask<?> task) throws Exception {
		cancel(task.getId());
	}

	public final static ScheduledTask<?> runAsync(Runnable task) {
		try {
			if (NiftyCore.isBungee()) {
				net.md_5.bungee.api.plugin.Plugin owner = (net.md_5.bungee.api.plugin.Plugin)NiftyCore.getPlugin();
				return runAsync(owner, task);
			}

			org.bukkit.plugin.java.JavaPlugin owner = (org.bukkit.plugin.java.JavaPlugin)NiftyCore.getPlugin();
			return runAsync(owner, task);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static ScheduledTask<?> runAsync(final Runnable task, final long delay) {
		try {
			if (NiftyCore.isBungee()) {
				return schedule(new Runnable() {
					@Override
					public void run() {
						runAsync(task).getId();
					}
				}, delay);
			}

			org.bukkit.plugin.java.JavaPlugin owner = (org.bukkit.plugin.java.JavaPlugin)NiftyCore.getPlugin();
			return runAsync(owner, task, delay);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static ScheduledTask<?> runAsync(final Runnable task, final long delay, final long period) {
		try {
			if (NiftyCore.isBungee()) {
				return schedule(new Runnable() {
					@Override
					public void run() {
						runAsync(task);
					}
				}, delay, period);
			}

			org.bukkit.plugin.java.JavaPlugin owner = (org.bukkit.plugin.java.JavaPlugin)NiftyCore.getPlugin();
			return runAsync(owner, task, delay, period);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static <T extends net.md_5.bungee.api.plugin.Plugin> ScheduledTask<T> runAsync(T owner, Runnable task) {
		try {
			//Object taskObj = SCHEDULER.invokeMethod(StringUtil.format("run{0}Async{1}", (IS_BUNGEE ? "" : "Task"), (IS_BUNGEE ? "" : "hronously")), SCHEDULER_OBJ, plugin, task);
			Object taskObj = SCHEDULER.invokeMethod("runAsync", SCHEDULER_OBJ, owner, task);
			int taskId = (int)BUNGEE_TASK.invokeMethod("getId", taskObj);
			return new ScheduledTask<T>(owner, taskId, false);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static <T extends org.bukkit.plugin.java.JavaPlugin> ScheduledTask<T> runAsync(T owner, Runnable task) {
		return runAsync(owner, task, 0);
	}

	public final static <T extends org.bukkit.plugin.java.JavaPlugin> ScheduledTask<T> runAsync(T owner, Runnable task, long delay) {
		try {
			Object taskObj = SCHEDULER.invokeMethod("runTaskLaterAsynchronously", SCHEDULER_OBJ, owner, task, delay);
			int taskId = (int)BUKKIT_TASK.invokeMethod("getTaskId", taskObj);
			return new ScheduledTask<T>(owner, taskId, false);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static <T extends net.md_5.bungee.api.plugin.Plugin> ScheduledTask<T> runAsync(T owner, final Runnable task, long delay, TimeUnit unit) {
		try {
			return schedule(owner, new Runnable() {
				@Override
				public void run() {
					runAsync(task);
				}
			}, delay, unit);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static <T extends org.bukkit.plugin.java.JavaPlugin> ScheduledTask<T> runAsync(T owner, Runnable task, long delay, long period) {
		try {
			Object taskObj = SCHEDULER.invokeMethod("runTaskTimerAsynchronously", SCHEDULER_OBJ, owner, task, delay, period);
			int taskId = (int)BUKKIT_TASK.invokeMethod("getTaskId", taskObj);
			return new ScheduledTask<T>(owner, taskId, false);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static <T extends net.md_5.bungee.api.plugin.Plugin> ScheduledTask<T> runAsync(T owner, final Runnable task, long delay, long period, TimeUnit unit) {
		try {
			return schedule(owner, new Runnable() {
				@Override
				public void run() {
					runAsync(task);
				}
			}, delay, period, unit);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static ScheduledTask<?> schedule(Runnable task) {
		try {
			if (NiftyCore.isBungee()) {
				net.md_5.bungee.api.plugin.Plugin owner = (net.md_5.bungee.api.plugin.Plugin)NiftyCore.getPlugin();
				return schedule(owner, task, 0, TimeUnit.MILLISECONDS);
			}

			org.bukkit.plugin.java.JavaPlugin owner = (org.bukkit.plugin.java.JavaPlugin)NiftyCore.getPlugin();
			return schedule(owner, task);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static <T extends org.bukkit.plugin.java.JavaPlugin> ScheduledTask<T> schedule(T owner, Runnable task) {
		try {
			Object taskObj = SCHEDULER.invokeMethod("scheduleSyncDelayedTask", SCHEDULER_OBJ, owner, task);
			int taskId = (int)BUKKIT_TASK.invokeMethod("getTaskId", taskObj);
			return new ScheduledTask<T>(owner, taskId, true);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static ScheduledTask<?> schedule(Runnable task, long delay) {
		try {
			if (NiftyCore.isBungee()) {
				net.md_5.bungee.api.plugin.Plugin owner = (net.md_5.bungee.api.plugin.Plugin)NiftyCore.getPlugin();
				return schedule(owner, task, delay, TimeUnit.MILLISECONDS);
			}

			org.bukkit.plugin.java.JavaPlugin owner = (org.bukkit.plugin.java.JavaPlugin)NiftyCore.getPlugin();
			return schedule(owner, task, delay);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static <T extends org.bukkit.plugin.java.JavaPlugin> ScheduledTask<T> schedule(T owner, Runnable task, long delay) {
		try {
			Object taskObj = SCHEDULER.invokeMethod("scheduleSyncDelayedTask", SCHEDULER_OBJ, owner, task, delay);
			int taskId = (int)BUKKIT_TASK.invokeMethod("getTaskId", taskObj);
			return new ScheduledTask<T>(owner, taskId, true);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static ScheduledTask<?> schedule(Runnable task, long delay, long period) {
		try {
			if (NiftyCore.isBungee()) {
				net.md_5.bungee.api.plugin.Plugin owner = (net.md_5.bungee.api.plugin.Plugin)NiftyCore.getPlugin();
				return schedule(owner, task, delay, period, TimeUnit.MILLISECONDS);
			}

			org.bukkit.plugin.java.JavaPlugin owner = (org.bukkit.plugin.java.JavaPlugin)NiftyCore.getPlugin();
			return schedule(owner, task, delay, period);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static <T extends org.bukkit.plugin.java.JavaPlugin> ScheduledTask<T> schedule(T owner, Runnable task, long delay, long period) {
		try {
			Object taskObj = SCHEDULER.invokeMethod("scheduleSyncRepeatingTask", SCHEDULER_OBJ, owner, task, delay, period);
			int taskId = (int)BUKKIT_TASK.invokeMethod("getTaskId", taskObj);
			return new ScheduledTask<T>(owner, taskId, true);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static <T extends net.md_5.bungee.api.plugin.Plugin> ScheduledTask<T> schedule(T owner, Runnable task, long delay, TimeUnit unit) {
		try {
			Object taskObj = SCHEDULER.invokeMethod("schedule", SCHEDULER_OBJ, owner, task, delay, unit);
			int taskId = (int)BUNGEE_TASK.invokeMethod("getId", taskObj);
			return new ScheduledTask<T>(owner, taskId, true);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public final static <T extends net.md_5.bungee.api.plugin.Plugin> ScheduledTask<T> schedule(T owner, Runnable task, long delay, long period, TimeUnit unit) {
		try {
			Object taskObj = SCHEDULER.invokeMethod("schedule", SCHEDULER_OBJ, owner, task, delay, period, unit);
			int taskId = (int)BUKKIT_TASK.invokeMethod("geTasktId", taskObj);
			return new ScheduledTask<T>(owner, taskId, true);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}