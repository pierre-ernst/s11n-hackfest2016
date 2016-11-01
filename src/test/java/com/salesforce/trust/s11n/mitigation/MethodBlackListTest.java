package com.salesforce.trust.s11n.mitigation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author Pierre Ernst
 *
 */
public class MethodBlackListTest {

	private static class ClassWithExploitableReadObject implements Serializable {

		private static final long serialVersionUID = -2221642578512182098L;
		private bonhomme.Carnaval bonhomme;

		private void readObject(ObjectInputStream in) {
			try {
				// looks like a "springboard" class
				bonhomme = (bonhomme.Carnaval) new ObjectInputStream(new ByteArrayInputStream(new byte[] { (byte) 0xAC,
						(byte) 0xED, 0, 5, (byte) 0x73, (byte) 0x72, 0, (byte) 0x11, (byte) 0x62, (byte) 0x6F,
						(byte) 0x6E, (byte) 0x68, (byte) 0x6F, (byte) 0x6D, (byte) 0x6D, (byte) 0x65, (byte) 0x2E,
						(byte) 0x43, (byte) 0x61, (byte) 0x72, (byte) 0x6E, (byte) 0x61, (byte) 0x76, (byte) 0x61,
						(byte) 0x6C, (byte) 0x20, (byte) 0x51, (byte) 0x75, (byte) 0x65, (byte) 0x62, (byte) 0x65,
						(byte) 0x63, (byte) 0x20, 2, 0, 0, (byte) 0x78, (byte) 0x70 })).readObject();
			} catch (ClassNotFoundException | IOException ex) {
				ex.printStackTrace(System.err);
			}
		}
	}

	private static class SubClassOfaSuperClassWithExploitableReadObject extends ClassWithExploitableReadObject
			implements Serializable {

		private static final long serialVersionUID = 8177046281162083074L;

		private void readObject(ObjectInputStream in) {
			System.out.println("This method is safe");
		}
	}

	private static class ClassWithExploitableFinalize implements Serializable {

		private static final long serialVersionUID = 4539005278501600484L;

		private void doStuff() throws IOException {
			new FileWriter("ernst").write("pierre"); // blacklisted
		}

		protected void finalize() throws Throwable {
			doStuff();
		}
	}

	private static class ClassWithExploitableValidateObject implements Serializable {

		private static final long serialVersionUID = -7550174823694807849L;

		private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
			ois.defaultReadObject();
			ois.registerValidation(new ObjectInputValidation() {

				@Override
				public void validateObject() throws InvalidObjectException {
					System.exit(0); // blacklisted
				}
			}, 0);
		}
	}

	private static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);
		byte[] buffer = baos.toByteArray();
		oos.close();
		baos.close();
		return buffer;
	}

	private static Object deserialize(byte[] buffer) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);

		// We use MethodBlackListObjectInputStream instead of
		// ObjectInputStream
		ObjectInputStream ois = new MethodBlackListObjectInputStream(bais);

		Object obj = ois.readObject();
		ois.close();
		bais.close();
		return obj;
	}

	@Test
	public void testLegitimateDeserialization() throws ClassNotFoundException, IOException {

		bonhomme.Carnaval bonhomme0 = new bonhomme.Carnaval();
		bonhomme.Carnaval bonhomme1 = (bonhomme.Carnaval) deserialize(serialize(bonhomme0));

		assertEquals("should be same", bonhomme0, bonhomme1);
	}

	@Test
	public void testClassWithExploitableReadObject() throws ClassNotFoundException, IOException {
		try {
			deserialize(serialize(new ClassWithExploitableReadObject()));
			fail("malicious deserialization not mitigated");
		} catch (ObjectStreamException expected) {
			assertTrue("unexpected exception",
					expected.getMessage().startsWith("Unauthorized deserialization attempt"));
		}
	}

	@Test
	public void testClassWithExploitableValidateObject() throws ClassNotFoundException, IOException {
		try {
			deserialize(serialize(new ClassWithExploitableValidateObject()));
			fail("malicious deserialization not mitigated");
		} catch (ObjectStreamException expected) {
			assertTrue("unexpected exception",
					expected.getMessage().startsWith("Unauthorized deserialization attempt"));
		}
	}

	@Test
	public void testSubClassOfaSuperClassWithExploitableReadObject() throws ClassNotFoundException, IOException {
		try {
			deserialize(serialize(new SubClassOfaSuperClassWithExploitableReadObject()));
			fail("malicious deserialization not mitigated");
		} catch (ObjectStreamException expected) {
			assertTrue("unexpected exception",
					expected.getMessage().startsWith("Unauthorized deserialization attempt"));
		}
	}

	@Test
	public void testClassWithExploitableFinalize() throws ClassNotFoundException, IOException {
		try {
			deserialize(serialize(new ClassWithExploitableFinalize()));
			fail("malicious deserialization not mitigated");
		} catch (ObjectStreamException expected) {
			assertTrue("unexpected exception",
					expected.getMessage().startsWith("Unauthorized deserialization attempt"));
		}
	}
}
