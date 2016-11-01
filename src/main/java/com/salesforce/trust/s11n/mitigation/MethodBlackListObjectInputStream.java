package com.salesforce.trust.s11n.mitigation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectStreamClass;
import java.lang.reflect.Method;

public class MethodBlackListObjectInputStream extends ObjectInputStream {

	private ClassIntrospector classIntrospector;

	public MethodBlackListObjectInputStream(InputStream inputStream) throws IOException {
		super(inputStream);
		this.classIntrospector = AbstractClassIntrospector.getInstance();
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {

		for (Method magicMethod : classIntrospector.getMagicMethods(Class.forName(desc.getName()))) {
			if (classIntrospector.isMagicMethodBlackListed(magicMethod)) {
				throw new InvalidClassException(
						"Unauthorized deserialization attempt - method " + magicMethod.getName() + "()",
						desc.getName());
			}
		}

		return super.resolveClass(desc);
	}

	@Override
	public void registerValidation(ObjectInputValidation oiv, int priority)
			throws NotActiveException, InvalidObjectException {
		try {
			Method magicMethod = oiv.getClass().getDeclaredMethod("validateObject", new Class<?>[0]);
			if (classIntrospector.isMagicMethodBlackListed(magicMethod)) {
				throw new InvalidObjectException(
						"Unauthorized deserialization attempt - method validateObject() - class "
								+ oiv.getClass().getName());
			}
			super.registerValidation(oiv, priority);
		} catch (NoSuchMethodException | SecurityException ex) {
			ex.printStackTrace(System.err);
		}
	}
}
