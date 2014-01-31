/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.hayde.box.template.util;

/**
 *
 * @author senturk
 */
public class NamedHashMap<T> extends java.util.HashMap<String, T> {

	private String name = null;

	public NamedHashMap(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
