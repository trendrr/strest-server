/**
 * 
 */
package com.trendrr.strest.server.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 
 * 
 * @author Dustin Norlander
 * @created Jan 14, 2011
 * 
 */
public class TreeNode {

	protected Log log = LogFactory.getLog(TreeNode.class);
	
	UriMapping mapping = null;
	HashMap<String, TreeNode> children = new HashMap<String,TreeNode>();
	List<TreeNode> wildcards = new ArrayList<TreeNode>();

	
	void addChildNode(UriMapping mapping, List<String> tokens) {
		String word = tokens.get(0);
		List<String> words = tokens.subList(1, tokens.size());
		TreeNode node = new TreeNode();
		if (children.containsKey(word)) {
			node = children.get(word);
		}
		
		if (word.startsWith(":")) {
			this.wildcards.add(node);
		} else {
			this.children.put(word, node);
		}
		
		if (words.isEmpty()) {
			node.setMapping(mapping);
		} else {
			node.addChildNode(mapping, words);
		}
	}
	
	public UriMapping getMapping() {
		return mapping;
	}

	public void setMapping(UriMapping mapping) {
		if (this.mapping != null) {
			log.warn("Overwriting mapping: " + this.mapping + " with mapping: " + mapping );	
		}
		this.mapping = mapping;
	}

	/**
	 * recursively search for matches
	 * @param found
	 * @param words
	 */
	public void find(List<UriMapping> found, List<String> words) {
		String word = "";
		if (!words.isEmpty())
			word = words.get(0);
		
		if (word.equals("")) {
			if (mapping != null) {
				found.add(mapping);
			}
			return;
		}
			
	
		List<String> wordList = words.subList(1, words.size());
		if (children.containsKey(word)) {
			children.get(word).find(found, wordList);
		}
		for (TreeNode n : this.wildcards) {
			n.find(found, wordList);
		}
		
	}
}
