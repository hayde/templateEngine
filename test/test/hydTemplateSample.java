/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import eu.hayde.box.template.Template;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author senturk
 */
public class hydTemplateSample {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws Exception {
		Template template = new Template("", "http://localhost:8080/lk_template/sample.html");

		List<Person> persons = new ArrayList<Person>();

		persons.add(new Person("Ahmet", "Hashim", new Profession("poet", 19)));
		persons.add(new Person("Sun", "Tzu", new Profession("mathematician", -5)));
		persons.add(new Person("Ṣalāḥ al-Dīn", "ibn Ayyūb", new Profession("sultan", 12)));

		template.addObject("persons", persons);
		template.addObject("nongrada", persons);

		int actions = template.process();

		System.out.println(template.getContent());

		System.out.println(template.getXML());
		System.out.println(template.getJSON());

		System.out.println(" Actions >> " + actions);
	}
}
