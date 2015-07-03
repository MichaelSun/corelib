package com.michael.corelib.internet.core;

import android.content.Context;

public class BeanRequestFactory {

	private static BeanRequestInterface gBeanRequestInterface;

	public synchronized static BeanRequestInterface createBeanRequestInterface(Context context) {
		if (gBeanRequestInterface == null) {
			gBeanRequestInterface = BeanRequestDefaultImplInternal.getInstance(context);
		}

		return gBeanRequestInterface;
	}

	public synchronized static void setgBeanRequestInterfaceImpl(BeanRequestInterface impl) {
		gBeanRequestInterface = impl;
	}
}
