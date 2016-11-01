package com.salesforce.trust.s11n.mitigation.bytecode;

import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.salesforce.trust.s11n.mitigation.AbstractClassIntrospector;

import serp.bytecode.BCClass;
import serp.bytecode.BCMethod;
import serp.bytecode.ClassInstruction;
import serp.bytecode.Code;
import serp.bytecode.Instruction;
import serp.bytecode.MethodInstruction;
import serp.bytecode.Project;

public class SerpClassIntrospector extends AbstractClassIntrospector {

	private Project project;
	private Set<BCMethod> blackListMethod;

	public SerpClassIntrospector() {
		this.project = new Project();
		this.blackListMethod = new HashSet<>();
		for (Method m : AbstractClassIntrospector.METHOD_BLACK_LIST) {
			BCClass bc = project.loadClass(m.getDeclaringClass());
			this.blackListMethod.add(bc.getDeclaredMethod(m.getName(), m.getParameterTypes()));
		}
	}

	@Override
	public boolean isMagicMethodBlackListed(Method mth) {
		BCClass bc = project.loadClass(mth.getDeclaringClass());
		BCMethod bm = bc.getDeclaredMethod(mth.getName(), mth.getParameterTypes());
		return isMethodBlackListed(bm);
	}

	private boolean isMethodBlackListed(BCMethod bm) {
		Code code = bm.getCode(true);

		for (Instruction mi : code.getInstructions()) {
			if (mi instanceof MethodInstruction) {
				BCMethod calledMethod = ((MethodInstruction) mi).getMethod();

				for (Class<?> cl : AbstractClassIntrospector.CLASS_BLACK_LIST) {
					if (cl.isAssignableFrom(calledMethod.getDeclarer().getType())) {
						return true;
					}
				}

				if (blackListMethod.contains(calledMethod)) {
					return true;
				}

				return isMethodBlackListed(calledMethod);

			} else if (mi instanceof ClassInstruction) {
				ClassInstruction ci = (ClassInstruction) mi;
				if (ci.getName().equals("new")) {
					String newClassName = ci.getTypeName();
					try {
						Class<?> newClass = Class.forName(newClassName);

						// Prevent "springboard classes" (see
						// https://goo.gl/rOpF0u)
						if (ObjectInputStream.class.isAssignableFrom(newClass)) {
							return true;
						}
					} catch (ClassNotFoundException ex) {
						ex.printStackTrace(System.err);
						return true; // fails securely
					}
				}
			}
		}

		return false;
	}
}
