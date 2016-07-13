package net.netcoding.nifty.core.api.scheduler;

public class ScheduledTask<T> {

	volatile int id = -1;
	private final T plugin;
	private final boolean sync;

	ScheduledTask(T plugin, int id, boolean sync) {
		this.plugin = plugin;
		this.id = id;
		this.sync = sync;
	}

    /**
     * Will attempt to cancel this task if running.
     */
	public final void cancel() throws Exception {
		MinecraftScheduler.getInstance().cancel(this);
	}

    /**
     * Returns the id for the task.
     *
     * @return Task id number.
     */
	public final int getId() {
		return this.id;
	}

    /**
     * Returns the Plugin that owns this task.
     *
     * @return The Plugin that owns the task.
     */
	public final T getOwner() {
		return this.plugin;
	}

    /**
     * Gets if the current task is a sync task.
     *
     * @return True if the task is run by main thread
     */
    public boolean isSync() {
    	return this.sync;
    }

}