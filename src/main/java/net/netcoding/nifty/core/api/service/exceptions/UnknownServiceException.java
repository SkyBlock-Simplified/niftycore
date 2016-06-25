package net.netcoding.nifty.core.api.service.exceptions;

import net.netcoding.nifty.core.util.StringUtil;

public final class UnknownServiceException extends IllegalArgumentException {

	public UnknownServiceException(Class<?> service) {
		super(StringUtil.format("Service ''{0}'' has not been registered!", service.getName()));
	}

}