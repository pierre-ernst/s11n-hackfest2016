package com.salesforce.trust.s11n.mitigation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Method;

public class ByteCodeBlackListObjectInputStream extends ObjectInputStream {
	
	private ClassIntrospector classIntrospector;
	
	public ByteCodeBlackListObjectInputStream(InputStream inputStream)
            throws IOException {
        super(inputStream);
        this.classIntrospector=AbstractClassIntrospector.getInstance();
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
            ClassNotFoundException {
    	
    	for (Method magicMethod: classIntrospector.getMagicMethods(Class.forName(desc.getName()))){
    		if (classIntrospector.isMagicMethodBlackListed(magicMethod)){
    			throw new InvalidClassException(
                        "Unauthorized deserialization attempt - method "+magicMethod.getName()+"()",
                        desc.getName());
    		}
    	}
    	
        return super.resolveClass(desc);
    }
}
