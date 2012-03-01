/**
 * 
 */
package com.trendrr.strest.annotations;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


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
}
