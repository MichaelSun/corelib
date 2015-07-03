package com.michael.corelib.internet.core;

import android.os.Bundle;

public interface BeanRequestInterface {

	public <T> T request(RequestBase<T> request) throws NetWorkException;

	public String getSig(Bundle params, String secret_key);
}
