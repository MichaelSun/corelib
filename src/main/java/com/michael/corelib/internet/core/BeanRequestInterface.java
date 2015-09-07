package com.michael.corelib.internet.core;

public interface BeanRequestInterface {

	<T> T request(RequestBase<T> request) throws NetWorkException;

}
