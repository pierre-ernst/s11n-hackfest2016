package com.salesforce.trust.s11n.mitigation;

import java.lang.reflect.Method;

public interface ClassIntrospector {

	public Method[] getMagicMethods(Class<?> cls);
	
	public boolean isMagicMethodBlackListed(Method mth);
}
