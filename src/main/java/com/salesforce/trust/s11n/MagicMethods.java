package com.salesforce.trust.s11n;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Test which "Magic" methods are called during deserialization
 * 
 * @author Pierre Ernst
 *
 */
public class MagicMethods {

	private static byte[] serialize(Object o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		byte[] stream = baos.toByteArray();
		oos.close();
		baos.close();
		return stream;
	}

	private static Object deserialize(byte[] buffer)
			throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object obj = ois.readObject();
		ois.close();
		bais.close();
		return obj;
	}

	private static class SerializableWithReadObject implements Serializable {

		private static final long serialVersionUID = 4856510662027268327L;

		private void readObject(ObjectInputStream ois) {
			System.out.println("readObject()");
		}
	}

	private static class SerializableSubclass extends SerializableWithReadObject implements Serializable {

		private static final long serialVersionUID = 6721194549787980194L;	
	}
	
	private static class SerializableWithReadResolve implements Serializable {

		private static final long serialVersionUID = -5392234127160929702L;

		private Object readResolve() throws ObjectStreamException {
			System.out.println("readResolve()");
			return "Pierre Ernst, Salesforce";
		}
	}

	private static class SerializableWithValidateObject implements Serializable {

		private static final long serialVersionUID = 7727388759136195190L;

		private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
			ois.defaultReadObject();
			ois.registerValidation(new ObjectInputValidation() {

				@Override
				public void validateObject() throws InvalidObjectException {
					System.out.println("validateObject()");
				}
			}, 0);
		}
	}

	private static class SerializableWithFinalize implements Serializable {

		private static final long serialVersionUID = -7665317786008115802L;

		@Override
		public void finalize() {
			System.out.println("finalize()");
		}
	}

	private static class SerializableWithNoDataSuperClass implements Serializable {

		private static final long serialVersionUID = 4045250292950505350L;

		private void readObjectNoData() throws ObjectStreamException {
			System.out.println("readObjectNoData()");
		}
	}

	private static class SerializableWithNoDataSubClass extends SerializableWithNoDataSuperClass
			implements Serializable {

		private static final long serialVersionUID = -4818397440078161621L;

	}

	private static class ExternalizableWithReadExternal implements Externalizable {

		public ExternalizableWithReadExternal() {
			// required for Externalizable
		}

		@Override
		public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
			System.out.println("readExternal()");
		}

		@Override
		public void writeExternal(ObjectOutput oo) throws IOException {
		}
	}

	public static void main(String... args) {
		try {
			
			deserialize(serialize(new SerializableWithReadObject()));

			deserialize(serialize(new SerializableSubclass()));
			
			deserialize(serialize(new SerializableWithReadResolve()));

			deserialize(serialize(new SerializableWithValidateObject()));

			Object o0 = new SerializableWithFinalize();
			Object o1 = deserialize(serialize(o0));
			o0 = o1 = null;
			// finalize() should be called twice, once on o0 and once on o1
			System.gc();
			System.out.println("Press enter...");
			System.in.read();

			// This instance of SerializableWithNoDataSubClass was serialized
			// when it did *not* extends SerializableWithNoDataSuperClass
			deserialize(new byte[] { (byte) 0xAC, (byte) 0xED, (byte) 0x00, (byte) 0x05, (byte) 0x73, (byte) 0x72,
					(byte) 0x00, (byte) 0x4B, (byte) 0x63, (byte) 0x6F, (byte) 0x6D, (byte) 0x2E, (byte) 0x73,
					(byte) 0x61, (byte) 0x6C, (byte) 0x65, (byte) 0x73, (byte) 0x66, (byte) 0x6F, (byte) 0x72,
					(byte) 0x63, (byte) 0x65, (byte) 0x2E, (byte) 0x74, (byte) 0x72, (byte) 0x75, (byte) 0x73,
					(byte) 0x74, (byte) 0x2E, (byte) 0x6D, (byte) 0x69, (byte) 0x74, (byte) 0x69, (byte) 0x67,
					(byte) 0x61, (byte) 0x74, (byte) 0x69, (byte) 0x6F, (byte) 0x6E, (byte) 0x2E, (byte) 0x4D,
					(byte) 0x61, (byte) 0x67, (byte) 0x69, (byte) 0x63, (byte) 0x4D, (byte) 0x65, (byte) 0x74,
					(byte) 0x68, (byte) 0x6F, (byte) 0x64, (byte) 0x73, (byte) 0x24, (byte) 0x53, (byte) 0x65,
					(byte) 0x72, (byte) 0x69, (byte) 0x61, (byte) 0x6C, (byte) 0x69, (byte) 0x7A, (byte) 0x61,
					(byte) 0x62, (byte) 0x6C, (byte) 0x65, (byte) 0x57, (byte) 0x69, (byte) 0x74, (byte) 0x68,
					(byte) 0x4E, (byte) 0x6F, (byte) 0x44, (byte) 0x61, (byte) 0x74, (byte) 0x61, (byte) 0x53,
					(byte) 0x75, (byte) 0x62, (byte) 0x43, (byte) 0x6C, (byte) 0x61, (byte) 0x73, (byte) 0x73,
					(byte) 0xBD, (byte) 0x21, (byte) 0x9D, (byte) 0x0E, (byte) 0xA5, (byte) 0x7D, (byte) 0x59,
					(byte) 0x2B, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x78, (byte) 0x70 });

			deserialize(serialize(new ExternalizableWithReadExternal()));

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
}