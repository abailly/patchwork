package oqube.bytes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A Helper class for converting string parameters to primitive
 * types
 *
 * @author Arnaud Bailly
 */
public class TypeHelper {

	/** hash table to store conversion constructors */
	private static java.util.Map constructormap = new java.util.HashMap();
	/** hash table to store conversion from string methods */
	private static java.util.Map conversionmap = new java.util.HashMap();
	/** hashtable for type names to class mapping */
	private static java.util.Map classmap = new java.util.HashMap();
	/** hash table to store conversion to string methods */
	private static java.util.Map externmap = new java.util.HashMap();

	/** static initilizer 
	put constructors for base types
	*/
	static {
		Class cls = Integer.class;
		try {
			Class[] ctorparam = new Class[] { java.lang.String.class };
			// type int
			cls = Integer.class;
			Constructor ctor = cls.getConstructor(ctorparam);
			constructormap.put(int.class, ctor);
			classmap.put("int", int.class);
			// type long
			cls = Long.class;
			ctor = cls.getConstructor(ctorparam);
			constructormap.put(long.class, ctor);
			classmap.put("long", long.class);
			// type float
			cls = Float.class;
			ctor = cls.getConstructor(ctorparam);
			constructormap.put(float.class, ctor);
			classmap.put("float", float.class);
			// type boolean
			cls = Boolean.class;
			ctor = cls.getConstructor(ctorparam);
			constructormap.put(boolean.class, ctor);
			classmap.put("boolean", boolean.class);
			// type double
			cls = Double.class;
			ctor = cls.getConstructor(ctorparam);
			constructormap.put(double.class, ctor);
			classmap.put("double", double.class);
			// type short
			cls = Short.class;
			ctor = cls.getConstructor(ctorparam);
			constructormap.put(short.class, ctor);
			classmap.put("short", short.class);
			// type byte
			cls = Byte.class;
			ctor = cls.getConstructor(ctorparam);
			constructormap.put(byte.class, ctor);
			classmap.put("byte", byte.class);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * A method to register a factory for a type
	 *
	 * @param cls a class object for which we gives a factory
	 * @param method a method object to invoke for constructing objects. This method must
	 * be static, takes one String parameter and returns objects of class cls
	 * @exception IllegalArgumentException if method or cls are invalid (null, not static...)
	 */
	public static void registerFactory(Class cls, Method method) {
		Class[] clsparms = new Class[] { java.lang.String.class };
		Class retcls = method.getReturnType();
		int mod = method.getModifiers();
		Class[] parms = method.getParameterTypes();
		if (!retcls.equals(cls)
			|| !Modifier.isStatic(mod)
			|| !Modifier.isPublic(mod)
			|| Modifier.isAbstract(mod)
			|| !java.util.Arrays.equals(parms, clsparms))
			throw new IllegalArgumentException("Invalid argument to method TypeHelper.registerFactory");
		conversionmap.put(cls, method);
	}

	/**
	 * A method to register conversion to string methods
	 *
	 * @param cls a class object
	 * @param method conversion method. Must take an argument of right class
	 */
	public static void registerExtern(Class cls, Method method) {
		Class[] clsparms = new Class[] { cls };
		Class retcls = method.getReturnType();
		int mod = method.getModifiers();
		Class[] parms = method.getParameterTypes();
		if (!retcls.equals(String.class)
			|| !Modifier.isStatic(mod)
			|| !Modifier.isPublic(mod)
			|| Modifier.isAbstract(mod)
			|| !java.util.Arrays.equals(parms, clsparms))
			throw new IllegalArgumentException("Invalid argument to method TypeHelper.registerExtern");
		externmap.put(cls, method);
	}

	/**
	 * Main method to convert an object to a string
	 * 
	 * @param o object to convert
	 * @return a String representation of object
	 */
	public static String toString(Object o) {
		// try to find a method in externmap
		Method m = (Method) externmap.get(o.getClass());
		if (m != null)
			try {
				return (String) m.invoke(null, new Object[] { o });
			} catch (Exception ex) {
				// intentionally left blank
			}
		// return standard toString
		return o.toString();
	}

	/**
	 * Main method to convert from a string given a class object
	 *
	 * @param cls a class name
	 * @param str a String to parse into an object of given class
	 */
	public static Object convert(String cls, String str)
		throws ClassNotFoundException {
		return convert(getClass(cls), str);
	}

	/**
	 * Main method to convert from a string given a class object
	 *
	 * @param cls a Class object
	 * @param str a String to parse into an object of given class
	 */
	public static Object convert(Class cls, String str) {
		Class[] clsparms = new Class[] { java.lang.String.class };
		// first look into hashtables
		Constructor ctor = (Constructor) constructormap.get(cls);
		if (ctor != null)
			return invokeCtor(ctor, str);
		Method meth = (Method) conversionmap.get(cls);
		if (meth != null)
			return invokeMethod(meth, str);
		// try to find a suitable constructor
		try {
			ctor = cls.getConstructor(clsparms);
			// store in hashtable
			constructormap.put(cls, ctor);
			return invokeCtor(ctor, str);
		} catch (Exception ex) {
			// intentionally left blank - 
		}
		// try to find a suitable method
		try {
			Method[] methods = cls.getMethods();
			// try to find a static method taking one string parameter and returning an object of class cls
			for (int i = 0; i < methods.length; i++) {
				Class retcls = methods[i].getReturnType();
				int mod = methods[i].getModifiers();
				Class[] parms = methods[i].getParameterTypes();
				if (!retcls.equals(cls)
					|| !Modifier.isStatic(mod)
					|| !java.util.Arrays.equals(parms, clsparms))
					continue;
				// found a method - hope it is OK !!!
				conversionmap.put(cls, methods[i]);
				return invokeMethod(methods[i], str);
			}
		} catch (Throwable t) {
			// intentionally left blank
		}
		// no conversion found
		return null;
	}

	private static Object invokeCtor(Constructor ctor, String str) {
		try {
			return ctor.newInstance(new Object[] { str });
		} catch (Throwable t) {
			//System.err.println("Error in constructor invocation with argument "+str+" : "+t.getMessage());
			return null;
		}
	}

	private static Object invokeMethod(Method meth, String str) {
		try {
			// assume method is static
			return meth.invoke(null, new Object[] { str });
		} catch (Throwable t) {
			//System.err.println("Error in method invocation with argument "+str+" : "+t.getMessage());
			return null;
		}
	}

	/**
	 * Returns the external representation of a class
	 * given its class object
	 *
	 * @param cls a Class object
	 * @return a String representing the array type
	 */
	public static String getExternalName(Class cls) {
		try {
			if (cls.isArray())
				return getExternalName(getComponentType(cls)) + "[]";
			return cls.getName();
		} catch (ClassNotFoundException ex) {
			return null;
		}
	}

	/**
	 * Retourne le nom interbne d'un type etant donne 
	 * son nom externe (i.e. nom Java -> nom JVM). LA validite du tyep 
	 * n'est pas verifiee
	 *
	 * @param name chaine identifiant un tyep
	 * @return equivalent JVM du nom
	 */
	public static String getInternalName(Class c) {
		if (c.isArray() && c.getComponentType().isPrimitive())
			return c.getName();
		String bare = c.getName();
		String newname = "";
		if (c.isPrimitive()) {
			if (bare.equals("int"))
				newname += 'I';
			else if (bare.equals("long"))
				newname += 'J';
			else if (bare.equals("float"))
				newname += 'F';
			else if (bare.equals("boolean"))
				newname += 'Z';
			else if (bare.equals("double"))
				newname += 'D';
			else if (bare.equals("byte"))
				newname += 'B';
			else if (bare.equals("short"))
				newname += 'S';
			else if (bare.equals("char"))
				newname += 'C';
		} else {
			if (c.isArray())
				newname = bare.replace('.', '/');
			else
				newname = 'L' + bare.replace('.', '/') + ';';
		}
		return newname;
	}

	// retrieve Class object for arrays
	// translate classxxx[] -> [Lclassxxx; 
	// from externale to internal signature
	private static Class getArrayClass(String name)
		throws ClassNotFoundException {
		String bare = name.substring(0, name.indexOf('['));
		String newname = "";
		int dim = 0;
		// count dimensions	
		for (int i = 0; i < name.length(); i++)
			if (name.charAt(i) == '[') {
				newname += '[';
				dim++;
			}
		// primitive type ?
		if (bare.equals("int"))
			newname += 'I';
		else if (bare.equals("long"))
			newname += 'J';
		else if (bare.equals("float"))
			newname += 'F';
		else if (bare.equals("boolean"))
			newname += 'Z';
		else if (bare.equals("double"))
			newname += 'D';
		else if (bare.equals("byte"))
			newname += 'B';
		else if (bare.equals("short"))
			newname += 'S';
		else if (bare.equals("char"))
			newname += 'C';
		else {
			// this a hack around a probleme in jdk1.4.1 
			// there is no way to retrieve a Class instance denoting
			// an array of objects with a name 
			// neither my.package.Myclass[] no [Lmy/package/Myclass; works
			Class base = Class.forName(bare);
			int[] dims = new int[dim];
			for (int i = 0; i < dims.length; i++)
				dims[i] = 0;
			Object o = java.lang.reflect.Array.newInstance(base, dims);
			return o.getClass();
		}
		// return class
		return Class.forName(newname);

	}

	/**
	 * returns a Class object representing the base component
	 * type of an array type. If cls is a multi-dimensionnal array Class, this method 
	 * returns an array Class one dimension under cls. If cls is not
	 * an array Class, this method returns cls
	 *
	 * @param cls a Class object
	 */
	public static Class getComponentType(Class cls)
		throws ClassNotFoundException {
		if (!cls.isArray())
			return cls;
		// size of array
		String cname = cls.getName().substring(1);

		switch (cname.charAt(0)) {
			case '[' : //multidim array
				return Class.forName(cname);
			case 'L' :
				return Class.forName(
					cname.substring(1, cname.length() - 1).replace('/', '.'));
			case 'I' :
				return int.class;
			case 'J' :
				return long.class;
			case 'Z' :
				return boolean.class;
			case 'F' :
				return float.class;
			case 'D' :
				return double.class;
			case 'C' :
				return char.class;
			case 'S' :
				return short.class;
			case 'B' :
				return byte.class;
		}
		// should never get there
		return null;
	}

	/**
	 * returns the Class object given a class name
	 * Mainly useful for primitive types
	 * @return a Class object
	 * @exception ClassNotFoundException if name is not defined
	 */
	public static Class getClass(String name) throws ClassNotFoundException {
		try {
			if (name.indexOf('[') >= 0)
				return getArrayClass(name);
			// primitive type ?
			Class cls = (Class) classmap.get(name);
			if (cls == null)
				cls = Class.forName(name);
			return cls;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ClassNotFoundException(ex.getMessage());
		}
	}

	/**
	 * Method countArguments.
	 * 
	 * Returns the number of slots the given method signature needs
	 * for its arguments.  
	 * 
	 * @param tsig a String which must be a valid signature for a java method
	 * @return int
	 */
	public static int countArguments(String sig) {
		int ret = 0;
		for (int i = 1; i< sig.length() && sig.charAt(i) != ')' ;)
			switch (sig.charAt(i)) {
				case 'D' :
				case 'J' :
					ret += 2;
					i++;
					break;
				case 'L' : // an object - skip i past ;
					ret += 1;
					i = sig.indexOf(';',i) + 1;
					break;
				case '[' : // an array
					ret += 1;
					while (sig.charAt(i) == '[')i++; // skip past arrays
					if (sig.charAt(i) == 'L')
						i = sig.indexOf(';',i) + 1;
					else
						i++;
					break;
				default :
					ret += 1;
					i++;
			}
		// done
		return ret;
	}

}
