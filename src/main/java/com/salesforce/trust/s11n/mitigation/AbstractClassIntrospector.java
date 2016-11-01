package com.salesforce.trust.s11n.mitigation;

import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import com.salesforce.trust.s11n.mitigation.bytecode.SerpClassIntrospector;

public abstract class AbstractClassIntrospector implements ClassIntrospector {

	public static final Class<?>[] CLASS_BLACK_LIST = new Class<?>[] { Writer.class, ProcessBuilder.class,
			ClassLoader.class, URLConnection.class };

	public static final Method[] METHOD_BLACK_LIST;
	static {
		METHOD_BLACK_LIST = new Method[1];
		try {
			METHOD_BLACK_LIST[0] = System.class.getDeclaredMethod("exit", new Class<?>[] { Integer.TYPE });
		} catch (NoSuchMethodException | SecurityException ex) {
			ex.printStackTrace(System.err);
		}
	}

	public Method[] getMagicMethods(Class<?> cls) {
		Set<Method> magicMethods = new HashSet<>();

		try {
			magicMethods.add(cls.getDeclaredMethod("readObject", new Class<?>[] { ObjectInputStream.class }));
		} catch (NoSuchMethodException ex) {
			// readObject() not declared
		}

		try {
			magicMethods.add(cls.getDeclaredMethod("readResolve", new Class<?>[] {}));
		} catch (NoSuchMethodException ex) {
			// readResolve() not declared
		}

		try {
			magicMethods.add(cls.getDeclaredMethod("finalize", new Class<?>[] {}));
		} catch (NoSuchMethodException ex) {
			// finalize() not declared
		}

		try {
			magicMethods.add(cls.getDeclaredMethod("readObjectNoData", new Class<?>[] {}));
		} catch (NoSuchMethodException ex) {
			// readObjectNoData() not declared
		}

		try {
			magicMethods.add(cls.getDeclaredMethod("readExternal", new Class<?>[] { ObjectInput.class }));
		} catch (NoSuchMethodException ex) {
			// readExternal() not declared
		}

		return magicMethods.toArray(new Method[0]);
	}

	public static ClassIntrospector getInstance() {
		try {
			Class.forName("serp.bytecode.Project");
			return new SerpClassIntrospector();
		} catch (ClassNotFoundException ex) {
			// Serp library not in the classpath
		}
		return null;
	}

	public abstract boolean isMagicMethodBlackListed(Method mth);
}
