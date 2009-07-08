package org.ddialliance.ddieditor.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;

/**
 * Reflection utility
 */
public class DdiEditorRefUtil {
	static final Log log = LogFactory.getLog(LogType.SYSTEM,
			DdiEditorRefUtil.class);

	/**
	 * Invoke a static method on a class
	 * 
	 * @param className
	 *            name of class
	 * @param methodName
	 *            name of method
	 * @param args
	 *            arguments to method
	 * @return method return
	 * @throws Exception
	 */
	public static Object invokeStaticMethod(String className,
			String methodName, Object... args) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Invoke method: " + methodName + " on object type: "
					+ className);
		}

		Class[] classArgs = null;
		if (args != null) {
			classArgs = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				classArgs[i] = args[i].getClass();
			}
		}

		Class c = Class.forName(className);
		Method m = c.getDeclaredMethod(methodName, classArgs);
		Object o = null;
		try {
			o = m.invoke(null, args);
		} catch (InvocationTargetException e) {
			throw e;
		}
		return o;
	}

	/**
	 * Invoke a method on an object
	 * 
	 * @param obj
	 *            to invoke on
	 * @param methodName
	 *            name of method
	 * @param useInterfaceParams
	 *            use interface names arguments
	 * @param args
	 *            arguments of type varargs
	 * @return method return
	 * @throws Exception
	 */
	public static Object invokeMethod(Object obj, String methodName,
			boolean useInterfaceParams, Object... args) throws Exception {
		try {
			Object returnObj = null;

			// sort out params
			Class[] params = null;
			if (args != null) {
				params = new Class[args.length];
				for (int i = 0; i < args.length; i++) {
					if (useInterfaceParams) {
						// hack to use interface names with params
						for (int j = 0; j < args[i].getClass().getInterfaces().length; j++) {
							params[i] = args[i].getClass().getInterfaces()[i];
						}
					} else {
						if (args[i]==null) {
							params[i] = null;
						} else {
							params[i] = args[i].getClass();							
						}
					}
				}
			}

			// retrieve method
			Method m = obj.getClass().getMethod(methodName, params);

			// execute method
			if (!m.isAccessible()) {
				m.setAccessible(true);
				returnObj = m.invoke(obj, args);
				m.setAccessible(false);
			} else {
				returnObj = m.invoke(obj, args);
			}

			if (log.isDebugEnabled()) {
				log.debug("Invoke method: " + methodName + " on object type: "
						+ obj.getClass().getName() + " result: " + returnObj);
			}
			return returnObj;
		} catch (NoSuchMethodException e) {
			throw e;
		} catch (IllegalAccessException e) {
			throw e;
		} catch (InvocationTargetException e) {
			throw e;
		}
	}
}
