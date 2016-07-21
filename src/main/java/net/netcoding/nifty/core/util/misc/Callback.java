package net.netcoding.nifty.core.util.misc;

public interface Callback<R, T extends Throwable> {

	void handle(R result, T error);

}