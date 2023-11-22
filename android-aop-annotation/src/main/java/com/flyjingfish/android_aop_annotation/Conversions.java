/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/


package com.flyjingfish.android_aop_annotation;

import java.util.HashMap;
import java.util.Map;

public final class Conversions {
	// Can't make instances of me
	private Conversions() {}

	// we might want to keep a cache of small integers around
	public static Object intObject(int i) {
		return i;
	}
	public static Object shortObject(short i) {
		return i;
	}
	public static Object byteObject(byte i) {
		return i;
	}
	public static Object charObject(char i) {
		return i;
	}
	public static Object longObject(long i) {
		return i;
	}
	public static Object floatObject(float i) {
		return i;
	}
	public static Object doubleObject(double i) {
		return i;
	}
	public static Object booleanObject(boolean i) {
		return i;
	}
	public static Object voidObject() {
		return null;
	}

	static Map<String,String> argsToObject = new HashMap<>();
	static Map<String,String> returnToValue = new HashMap<>();
	static {
		argsToObject.put("I","intObject(%1$s)");
		argsToObject.put("S","shortObject(%1$s)");
		argsToObject.put("B","byteObject(%1$s)");
		argsToObject.put("C","charObject(%1$s)");
		argsToObject.put("J","longObject(%1$s)");
		argsToObject.put("F","floatObject(%1$s)");
		argsToObject.put("D","doubleObject(%1$s)");
		argsToObject.put("Z","booleanObject(%1$s)");

		returnToValue.put("I","intValue(%1$s)");
		returnToValue.put("S","shortValue(%1$s)");
		returnToValue.put("B","byteValue(%1$s)");
		returnToValue.put("C","charValue(%1$s)");
		returnToValue.put("J","longValue(%1$s)");
		returnToValue.put("F","floatValue(%1$s)");
		returnToValue.put("D","doubleValue(%1$s)");
		returnToValue.put("Z","booleanValue(%1$s)");
	}

	public static String getArgsXObject(String key){
		String value = argsToObject.get(key);
		if (value == null){
			value = "%1$s";
		}else {
			value = "Conversions."+value;
		}
		return value;
	}
	public static String getReturnXObject(String key){
		String value = returnToValue.get(key);
		if (value == null){
			value = "return %1$s";
		}else {
			value = "return Conversions."+value;
		}
		return value;
	}



	public static int intValue(Object o) {
		if (o == null) {
			return 0;
		} else if (o instanceof Number) {
			return ((Number)o).intValue();
		} else {
			throw new ClassCastException(o.getClass().getName() +
					" can not be converted to int");
		}
	}
	public static long longValue(Object o) {
		if (o == null) {
			return 0;
		} else if (o instanceof Number) {
			return ((Number)o).longValue();
		} else {
			throw new ClassCastException(o.getClass().getName() +
					" can not be converted to long");
		}
	}
	public static float floatValue(Object o) {
		if (o == null) {
			return 0;
		} else if (o instanceof Number) {
			return ((Number)o).floatValue();
		} else {
			throw new ClassCastException(o.getClass().getName() +
					" can not be converted to float");
		}
	}
	public static double doubleValue(Object o) {
		if (o == null) {
			return 0;
		} else if (o instanceof Number) {
			return ((Number)o).doubleValue();
		} else {
			throw new ClassCastException(o.getClass().getName() +
					" can not be converted to double");
		}
	}
	public static byte byteValue(Object o) {
		if (o == null) {
			return 0;
		} else if (o instanceof Number) {
			return ((Number)o).byteValue();
		} else {
			throw new ClassCastException(o.getClass().getName() +
					" can not be converted to byte");
		}
	}
	public static short shortValue(Object o) {
		if (o == null) {
			return 0;
		} else if (o instanceof Number) {
			return ((Number)o).shortValue();
		} else {
			throw new ClassCastException(o.getClass().getName() +
					" can not be converted to short");
		}
	}
	public static char charValue(Object o) {
		if (o == null) {
			return 0;
		} else if (o instanceof Character) {
			return (Character) o;
		} else {
			throw new ClassCastException(o.getClass().getName() +
					" can not be converted to char");
		}
	}
	public static boolean booleanValue(Object o) {
		if (o == null) {
			return false;
		} else if (o instanceof Boolean) {
			return (Boolean) o;
		} else {
			throw new ClassCastException(o.getClass().getName() +
					" can not be converted to boolean");
		}
	}

	// identity function for now.  This is not typed to "void" because we happen
	// to know that in Java, any void context (i.e., {@link ExprStmt})
	// can also handle a return value.
	public static Object voidValue(Object o) {
		if (o == null) {
			return o;
		} else {
			// !!! this may be an error in the future
			return o;
		}
	}
}
