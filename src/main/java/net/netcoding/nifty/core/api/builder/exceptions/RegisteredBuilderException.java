package net.netcoding.nifty.core.api.builder.exceptions;

import net.netcoding.nifty.core.util.StringUtil;

public final class RegisteredBuilderException extends UnsupportedOperationException {

	public RegisteredBuilderException(Class<?> service) {
		super(StringUtil.format("Builder ''{0}'' already registered!", service.getName()));
	}

}