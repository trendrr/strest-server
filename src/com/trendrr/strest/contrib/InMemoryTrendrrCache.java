/**
 * 
 */
package com.trendrr.strest.contrib;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.cache.TrendrrCache;


/**
 * TODO: this is not an actual implementation.  just a placeholder.  Implement via google guava lib.
 * 
 * 
 * @author Dustin Norlander
 * @created Jan 3, 2012
 * 
 */
public class InMemoryTrendrrCache extends TrendrrCache {

	/**
	 * @param config
	 */
	public InMemoryTrendrrCache(DynMap config) {
		super(config);
	}

	protected Log log = LogFactory.getLog(InMemoryTrendrrCache.class);

	/* (non-Javadoc)
	 * @see com.trendrr.oss.cache.TrendrrCache#_init(com.trendrr.oss.DynMap)
	 */
	@Override
	protected void _init(DynMap config) {
		log.warn("Initing cache");
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.cache.TrendrrCache#_set(java.lang.String, java.lang.Object, java.util.Date)
	 */
	@Override
	protected void _set(String key, Object obj, Date expires) {
		log.warn("saving key: " +key + " -- uhh, just kidding :)");
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.cache.TrendrrCache#_get(java.lang.String)
	 */
	@Override
	protected Object _get(String key) {
		log.warn("getting key: " +key + " -- uhh, just kidding :)");
		return null;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.cache.TrendrrCache#_inc(java.lang.String, java.lang.Number)
	 */
	@Override
	protected void _inc(String key, Number value) {
		log.warn("inc key: " +key + " -- uhh, just kidding :)");
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.cache.TrendrrCache#_addToSet(java.util.Collection)
	 */
	@Override
	protected Set<String> _addToSet(String key, Collection<String> str) {
		log.warn("saving set: " + key + " -- uhh, just kidding :)");
		return null;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.cache.TrendrrCache#_removeFromSet(java.util.Collection)
	 */
	@Override
	protected Set<String> _removeFromSet(String key, Collection<String> str) {
		log.warn("remove from set: " + key + " -- uhh, just kidding :)");
		return null;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.cache.TrendrrCache#_setIfAbsent(java.lang.String, java.lang.Object, java.util.Date)
	 */
	@Override
	protected boolean _setIfAbsent(String key, Object value, Date expires) {
		log.warn("setting if absent: " + key + " -- uhh, just kidding :)");
		
		return false;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.cache.TrendrrCache#_del(java.lang.String)
	 */
	@Override
	protected void _del(String key) {
		log.warn("deleting: " + key + " -- uhh, just kidding :)");
	}

	/* (non-Javadoc)
	 * @see com.trendrr.oss.cache.TrendrrCache#_getMulti(java.util.Collection)
	 */
	@Override
	protected Map<String, Object> _getMulti(Collection<String> keys) {
		log.warn("getting multi: " + keys + " -- uhh, just kidding :)");
		return null;
	}
}
