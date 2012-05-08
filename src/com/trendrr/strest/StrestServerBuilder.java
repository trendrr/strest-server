/**
 * 
 */
package com.trendrr.strest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.Yaml;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.DynMapFactory;
import com.trendrr.oss.FileHelper;
import com.trendrr.strest.server.StrestServer;


/**
 * @author Dustin Norlander
 * @created Mar 1, 2012
 * 
 */
public class StrestServerBuilder {

	protected Log log = LogFactory.getLog(StrestServerBuilder.class);
	
	DynMap config = new DynMap();
	StrestServer server = new StrestServer();
	Set<String> filterNamespaces = new HashSet<String>();
	
	/**
	 * build the server instance and return it.
	 * @return
	 * @throws Exception
	 */
	public StrestServer build() throws Exception {
		server.initialize(config);
		for (String nm : this.filterNamespaces) {
			server.getRouter().setFilters(nm, config.getList(String.class, nm +".filters"));
		}
		return server;
	}
	
	/**
	 * adds a config file.  will merge with any previous config files added
	 * 
	 * Config file is assumed to be either 
     * json or yaml.  See example_config.yaml for more.
	 * 
	 * if more then one filename is passed, the subsequent config files will override any fields in the previous ones.
	 * this allows you to chain config files
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public StrestServerBuilder addConfigFile(String filename) throws Exception {
		DynMap conf = this.dynMapFromFile(filename);
		if (conf == null) {
			log.warn("Unable to parse config file: " + filename);
			return this;
		}
		return this.addConfig(conf);
	}
	
	/**
	 * adds a config file.  will merge with any previous configuration options added
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public StrestServerBuilder addConfig(DynMap config) throws Exception {
		//test for namespaces
		for (String key: config.keySet()) {
			DynMap mp = config.getMap(key); //this isn't very fast, but I don't think it needs to be.
			if (mp == null)
				continue;
			if (mp.getList(String.class, "filters") != null) {
				this.filterNamespaces.add(key);
			}
		}
		this.config.extend(config);
		return this;
	}
	
	/**
	 * add a filter to be executed for the given namespace (usually namespace is either default,html,api)
	 * @param namespace
	 * @param filter should be fully qualified classname ex: "com.trendrr.cheshire.filters.SessionFilter"
	 * @return
	 */
	public StrestServerBuilder addFilters(String namespace, String ...filters) {
		this.config.addToListWithDot(namespace + ".filters", (java.lang.Object[])filters);
		this.filterNamespaces.add(namespace);
		return this;
	}
	/**
	 * add filters. these will be applied to all controllers.
	 * @param filters
	 * @return
	 */
	public StrestServerBuilder addFilter(String ...filters) {
		this.config.addToListWithDot("filters", (java.lang.Object[])filters);
		return this;
	}
	
	/**
	 * add controller packages.  these will be recursively searched for controller instances.
	 * @param filters
	 * @return
	 */
	public StrestServerBuilder addControllerPackage(String ...packages) {
		this.config.addToListWithDot("controller_packages", (java.lang.Object[])packages);
		return this;
	}
	
	public StrestServerBuilder port(int port) {
		this.config.putWithDot("default.port", port);
		return this;
	}
	
	/**
	 * number of io threads to use.  usually 2*number of cores.
	 * @param numThreads
	 * @return
	 */
	public StrestServerBuilder numThreadsIO(int numThreads) {
		this.config.putWithDot("threads.io", numThreads);
		return this;
	}
	
	/**
	 * number of worker threads.  adjust based on amount of blocking io done in controllers
	 * @param numThreads
	 * @return
	 */
	public StrestServerBuilder numThreadsWorker(int numThreads) {
		this.config.putWithDot("threads.worker", numThreads);
		return this;
	}
	
	private DynMap dynMapFromFile(String filename) throws Exception {
		if (filename.endsWith("yaml")) {
			Yaml yaml = new Yaml();
		    String document = FileHelper.loadString(filename);
		    Map map = (Map) yaml.load(document);
		    return DynMapFactory.instance(map);
		    
		} else if (filename.endsWith("xml")) {
			//TODO (or not..)
			
		}

		//assume json
		return DynMapFactory.instanceFromFile(filename);
		
	}
}
