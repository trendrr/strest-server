/**
 * 
 */
package com.trendrr.strest.examples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.strest.annotations.Strest;


/**
 * @author Dustin Norlander
 * @created Feb 29, 2012
 * 
 */
public class Test {

	protected Log log = LogFactory.getLog(Test.class);
	
	public static void main(String ...strings) {
		for (int i=0; i < 10; i++) {
			System.out.println(	new HelloWorld().getClass().getAnnotation(Strest.class));
		}
	}
}
