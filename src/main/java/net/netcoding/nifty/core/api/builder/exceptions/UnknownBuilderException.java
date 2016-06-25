package net.netcoding.nifty.core.api.builder.exceptions;

import net.netcoding.nifty.core.util.StringUtil;

public final class UnknownBuilderException extends IllegalArgumentException {

	public UnknownBuilderException(Class<?> service) {
		super(StringUtil.format("Builder ''{0}'' has not been registered!", service.getName()));
	}

}