/**
 * 
 */
package com.trendrr.strest.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.Reflection;
import com.trendrr.oss.TypeCast;


/**
 * @author Dustin Norlander
 * @created Feb 29, 2012
 * 
 */
public class AnnotationHelper {

	protected Log log = LogFactory.getLog(AnnotationHelper.class);
	
	/**
	 * check the requested method for the requested annotation.
	 * 
	 * TODO: we should cache the results, instead of having to do this loop on every pass.
	 * @param annotation
	 * @param obj
	 * @param methodName
	 * @return
	 */
	public static boolean hasMethodAnnotation(Class annotation, Object obj, String methodName) {
		 for (Method m : obj.getClass().getMethods()) {
			 if (m.getName().equals(methodName)) {
				 return m.isAnnotationPresent(annotation);
			 }
	      }
		 return false;
	}
	
	public static <T> T getAnnotationVal(Class annotation, Object obj, Class<T> cls, String name) {
		
		try {
			return TypeCast.cast(cls, obj.getClass().getAnnotation(annotation).getClass().getMethod(name).invoke(obj));
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
