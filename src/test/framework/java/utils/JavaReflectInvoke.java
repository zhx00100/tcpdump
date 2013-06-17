package test.framework.java.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Java 反射调用工具类
 * @author zhangxin11
 *
 */
public class JavaReflectInvoke {

	/**
	 * 获取静态成员变量
	 * @param classToCall
	 * @param classLevel
	 * @param fieldName
	 * @return
	 * @throws Exception
	 */
	
	public static Object invokeGetStaticProperty(Class<?> classToCall,
			int classLevel, String fieldName) throws Exception {

		try {
			Field field = classToCall.getDeclaredField(fieldName);
			
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			
			Object property = field.get(classToCall);
			
			return property;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 设置静态成员变量
	 * @param classToCall
	 * @param classLevel
	 * @param fieldName
	 * @param value
	 * @throws Exception
	 */
	public static void invokeSetStaticProperty(Class<?> classToCall,
			int classLevel, String fieldName, Object value) throws Exception {

		try {
			Field field = classToCall.getDeclaredField(fieldName);
			
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			field.set(classToCall, value);
			
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 调用静态方法
	 * @param classToCall
	 * @param classLevel
	 * @param methodName
	 * @param parameterTypes
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	public static Object invokeStaticMethod(Class<?> classToCall,
			int classLevel, String methodName, Class<?>[] parameterTypes,
			Object[] parameters) throws Exception {
		try {
			Method method = classToCall.getDeclaredMethod(methodName,
					parameterTypes);
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			Object result = method.invoke(classToCall, parameters);
			return result;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 调用类的实例方法
	 * @param instance
	 * @param classLevel
	 * @param methodName
	 * @param parameterTypes
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	public static Object invokeObjectMethod(Object instance, int classLevel,
			String methodName, Class<?>[] parameterTypes, Object[] parameters)
			throws Exception {
		// get class
		Class<?> classToCall = getClassToCall(instance, classLevel);

		// get method and invoke
		try {
			Method method = classToCall.getDeclaredMethod(methodName,
					parameterTypes);
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			Object result = method.invoke(instance, parameters);
			return result;
		} catch (InvocationTargetException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 获取类的实例成员变量
	 * @param instance
	 * @param classLevel
	 * @param fieldName
	 * @return
	 * @throws Exception
	 */
	public static Object getObjectProperty(Object instance, int classLevel,
			String fieldName) throws Exception {
		// get class
		Class<?> classToCall = getClassToCall(instance, classLevel);

		// get property
		try {
			Field field = classToCall.getDeclaredField(fieldName);
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			Object property = field.get(instance);
			return property;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 设置类的实例成员变量
	 * @param instance
	 * @param classLevel
	 * @param fieldName
	 * @param value
	 * @throws Exception
	 */
	public static void setObjectProperty(Object instance, int classLevel,
			String fieldName, Object value) throws Exception {
		// get class
		Class<?> classToCall = getClassToCall(instance, classLevel);

		// get property
		try {
			Field field = classToCall.getDeclaredField(fieldName);
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
			field.set(instance, value);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 递归调用
	 * @param instance
	 * @param classLevel 0 类本身, 1 it's 父类, 2 ...
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
