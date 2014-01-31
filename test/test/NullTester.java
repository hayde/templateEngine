/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import eu.hayde.box.template.Template;
import eu.hayde.box.template.TemplateException;
import eu.hayde.box.template.xml.XMLException;

/**
 *
 * @author senturk
 */
public class NullTester {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws TemplateException, XMLException {
		Template template = new Template("<span hyd:content='content==void'></span>");
		template.process();
		System.out.println(template.getContent());
	}
}
