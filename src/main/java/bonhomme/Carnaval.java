package bonhomme;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import org.apache.commons.io.HexDump;

import com.salesforce.trust.s11n.Common;

/**
 * Generates an Hex dump of a simple class
 * 
 * @author Pierre Ernst
 *
 */
public class Carnaval implements Serializable {

	private static final long serialVersionUID;
	static {
		serialVersionUID = bytesToLong(" Quebec ".getBytes());
	}

	/**
	 * http://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-
	 * and-back-in-java
	 */
	private static long bytesToLong(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.put(bytes);
		buffer.flip();
		return buffer.getLong();
	}


	public static void main(String... args) {
		try {
			byte[] stream = Common.serialize(new Carnaval());

			HexDump.dump(stream, 0, System.out, 0);

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
}
