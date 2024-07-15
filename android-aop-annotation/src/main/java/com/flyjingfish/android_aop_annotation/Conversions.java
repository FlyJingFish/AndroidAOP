package com.flyjingfish.android_aop_annotation;

import java.util.regex.Pattern;

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

	public static String stringValue(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof String) {
			return (String) o;
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

	public static Class<?> getClass_(String className) throws ClassNotFoundException {
		if ("int".equals(className)){
			return int.class;
		}else if ("short".equals(className)){
			return short.class;
		}else if ("byte".equals(className)){
			return byte.class;
		}else if ("char".equals(className)){
			return char.class;
		}else if ("long".equals(className)){
			return long.class;
		}else if ("float".equals(className)){
			return float.class;
		}else if ("double".equals(className)){
			return double.class;
		}else if ("boolean".equals(className)){
			return boolean.class;
		}else if (classnameArrayPattern.matcher(className).find()){
			return Class.forName(getArrayClazzName(className));
		}else {
			return Class.forName(className);
		}
	}

	private static final Pattern classnameArrayPattern = Pattern.compile("\\[\\]");
	private static String getArrayClazzName(String classname) {
		String subStr = "[]";
		int count = 0;
		int index = 0;

		while ((index = classname.indexOf(subStr, index)) != -1) {
			index += subStr.length();
			count++;
		}

		return "[".repeat(count) +
				getTypeInternal(classnameArrayPattern.matcher(classname).replaceAll(""));
	}

	private static String getTypeInternal(final String classname) {
		switch (classname) {
			case "boolean":
				return "Z";
			case "char":
				return "C";
			case "byte":
				return "B";
			case "short":
				return "S";
			case "int":
				return "I";
			case "float":
				return "F";
			case "long":
				return "J";
			case "double":
				return "D";
			default:
				return "L"+classname+";";
		}
	}

	static Object return2Type(Class<?> class_,Object value){
		if (class_==Integer.class){
			return intValue(value);
		}else if (class_==Short.class){
			return shortValue(value);
		}else if (class_==Byte.class){
			return byteValue(value);
		}else if (class_==Character.class){
			return charValue(value);
		}else if (class_==Long.class){
			return longValue(value);
		}else if (class_==Float.class){
			return floatValue(value);
		}else if (class_==Double.class){
			return doubleValue(value);
		}else if (class_==Boolean.class){
			return booleanValue(value);
		}else {
			return value;
		}
	}


	static Class<?> getReturnClass(String className){
		try {
			if (className.startsWith("[")){
				return Class.forName(className);
			}else if (className.startsWith("L")){
				return Class.forName(className.substring(1,className.length()-1));
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
}
