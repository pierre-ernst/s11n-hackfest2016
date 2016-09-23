package com.salesforce.trust.s11n;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;


/**
 * 
 * @author Pierre Ernst
 *
 */
public class Common {

	public static byte[] serialize(Object o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		byte[] stream = baos.toByteArray();
		oos.close();
		baos.close();
		return stream;
	}
	
	public static Object deserialize(byte[] buffer) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object obj = ois.readObject();
		ois.close();
		bais.close();
		return obj;
	}

	public static void setFieldValue(Class<?> cl, String fieldName, Object instance, Object fieldValue)
			throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException {
		Field f = cl.getDeclaredField(fieldName);
		f.setAccessible(true);
		f.set(instance, fieldValue);
	}
}
