package net.netcoding.nifty.core.api.service.exceptions;

import net.netcoding.nifty.core.util.StringUtil;

public final class RegisteredServiceException extends UnsupportedOperationException {

	public RegisteredServiceException(Class<?> service) {
		super(StringUtil.format("Service ''{0}'' already registered!", service.getName()));
	}

}