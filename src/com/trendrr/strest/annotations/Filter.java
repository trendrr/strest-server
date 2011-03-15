/**
 * 
 */
package com.trendrr.strest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dustin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Filter {
	
	
}
