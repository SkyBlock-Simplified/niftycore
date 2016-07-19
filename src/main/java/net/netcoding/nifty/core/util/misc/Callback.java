package net.netcoding.nifty.core.util.misc;

public interface Callback<T> {

	void handle(T result, Throwable error);

}