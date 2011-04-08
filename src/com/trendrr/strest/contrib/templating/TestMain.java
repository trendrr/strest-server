/**
 * 
 */
package com.trendrr.strest.contrib.templating;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stringtemplate.v4.ST;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.FileHelper;


/**
 * @author Dustin Norlander
 * @created Apr 7, 2011
 * 
 */
public class TestMain {

	protected Log log = LogFactory.getLog(TestMain.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		DynMap vals = new DynMap();
		vals.put("name", "Dustin");
		
		DynMap sub = new DynMap();
		sub.put("blah", "BLARG");
		vals.put("sub", sub);
//		FileHelper.loadString(filename)
		ST hello = new ST("Hello, {val.sub.blah}", '{', '}');
        hello.add("val", vals);

//		hello.add("name", "World");
        System.out.println(hello.render());

	}
}
