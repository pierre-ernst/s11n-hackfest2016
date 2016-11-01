package com.salesforce.trust.s11n.mitigation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * http://www.ibm.com/developerworks/library/se-lookahead/
 * 
 * @author Pierre Ernst
 *
 */
public class LookAheadObjectInputStream extends ObjectInputStream {

	public LookAheadObjectInputStream(InputStream inputStream) throws IOException {
		super(inputStream);
	}

	/**
	 * Only deserialize instances of our expected class
	 */
	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		if (!desc.getName().equals("bonhomme.Carnaval")) {
			throw new InvalidClassException("Unauthorized deserialization attempt", desc.getName());
		}
		return super.resolveClass(desc);
	}
}
