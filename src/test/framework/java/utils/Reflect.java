package test.framework.java.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Java 反射调用工具类
 * 
 * @author zhangxin11
 * 
 */
public class Reflect {

	/**
	 * 获取静态成员变量
	 * 
	 * @param classToCall
	 * @param classLevel
	 * @param fieldName
	 * @return
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws Exception
	 */
	public static Object invokeGetStaticProperty(Class<?> classToCall,
			int classLevel, String fieldName) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

		Field field = classToCall.getDeclaredField(fieldName);

		if (!field.isAccessible()) {
			field.setAccessible(true);
		}

		Object property = field.get(classToCall);

		return property;

	}

	/**
	 * 设置静态成员变量
	 * 
	 * @param classToCall
	 * @param classLevel
	 * @param fieldName
	 * @param value
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws Exception
	 */
	public static void invokeSetStaticProperty(Class<?> classToCall,
			int classLevel, String fieldName, Object value)
			throws NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {

		Field field = classToCall.getDeclaredField(fieldName);

		if (!field.isAccessible()) {
			field.setAccessible(true);
		}

		field.set(classToCall, value);
	}

	/**
	 * 调用静态方法
	 * 
	 * @param classToCall
	 * @param classLevel
	 * @param methodName
	 * @param parameterTypes
	 * @param parameters
	 * @return
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws Exception
	 */
	public static Object invokeStaticMethod(Class<?> classToCall,
			int classLevel, String methodName, Class<?>[] parameterTypes,
			Object[] parameters) throws NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {

		Method method = classToCall.getDeclaredMethod(methodName,
				parameterTypes);
		if (!method.isAccessible()) {
			method.setAccessible(true);
		}
		Object result = method.invoke(classToCall, parameters);
		return result;
	}

	/**
	 * 调用类的实例方法
	 * 
	 * @param instance
	 * @param classLevel
	 * @param methodName
	 * @param parameterTypes
	 * @param parameters
	 * @return
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws Exception
	 */
	public static Object invokeObjectMethod(Object instance, int classLevel,
			String methodName, Class<?>[] parameterTypes, Object[] parameters)
			throws NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		// get class
		Class<?> classToCall = getClassToCall(instance, classLevel);

		// get method and invoke
		Method method = classToCall.getDeclaredMethod(methodName,
				parameterTypes);
		if (!method.isAccessible()) {
			method.setAccessible(true);
		}
		Object result = method.invoke(instance, parameters);
		return result;
	}

	/**
	 * 获取类的实例成员变量
	 * 
	 * @param instance
	 * @param classLevel
	 * @param fieldName
	 * @return
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws Exception
	 */
	public static Object getObjectProperty(Object instance, int classLevel,
			String fieldName) throws NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		// get class
		Class<?> classToCall = getClassToCall(instance, classLevel);

		// get property
		Field field = classToCall.getDeclaredField(fieldName);
		if (!field.isAccessible()) {
			field.setAccessible(true);
		}
		Object property = field.get(instance);
		return property;
	}

	/**
	 * 设置类的实例成员变量
	 * 
	 * @param instance
	 * @param classLevel
	 * @param fieldName
	 * @param value
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws Exception
	 */
	public static void setObjectProperty(Object instance, int classLevel,
			String fieldName, Object value) throws NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		// get class
		Class<?> classToCall = getClassToCall(instance, classLevel);

		// get property
		Field field = classToCall.getDeclaredField(fieldName);
		if (!field.isAccessible()) {
			field.setAccessible(true);
		}
		field.set(instance, value);
	}

	/**
	 * 递归调用
	 * 
	 * @param instance
	 * @param classLevel
	 *            0 类本身, 1 it's 父类, 2 ...
	 * @return
	 */
	private static Class<?> getClassToCall(Object instance, int classLevel) {
		Class<?> classToCall = instance.getClass();
		for (int i = 0; i < classLevel; i++) {
			classToCall = classToCall.getSuperclass();
		}
		return classToCall;
	}

}
