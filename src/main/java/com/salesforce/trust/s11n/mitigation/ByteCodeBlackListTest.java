package com.salesforce.trust.s11n.mitigation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 
 * @author Pierre Ernst
 *
 */
public class ByteCodeBlackListTest {

	private static class ClassWithExploitableReadObject implements Serializable {

		private static final long serialVersionUID = -2221642578512182098L;

		private void readObject(ObjectInputStream in) {
			System.exit(0);
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
			new FileWriter("ernst").write("pierre");
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
					System.exit(0);
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

		// We use ByteCodeBlackListObjectInputStream instead of
		// ObjectInputStream
		ObjectInputStream ois = new ByteCodeBlackListObjectInputStream(bais);

		Object obj = ois.readObject();
		ois.close();
		bais.close();
		return obj;
	}

	public static void main(String... args) {
		try {
			// Deserialize the Float instance (legitimate use case)
			Float f = (Float) deserialize(serialize(666f));
			System.out.println(f + " has been deserialized.");

			deserialize(serialize(new ClassWithExploitableValidateObject()));  //TODO fix this!
			//deserialize(serialize(new SubClassOfaSuperClassWithExploitableReadObject()));
			// deserialize(serialize(new ClassWithExploitableReadObject()));
			// deserialize(serialize(new ClassWithExploitableFinalize()));

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
}
