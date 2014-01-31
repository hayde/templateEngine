package test;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import eu.hayde.box.template.Template;
import eu.hayde.box.template.TemplateException;
import eu.hayde.box.template.xml.XMLException;
import java.net.URISyntaxException;
import test.BashSamples.TheClass;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author senturk
 */
public class templateTester {

	public static class Person {

		public String aberHallo;
		public int aberSo;
		public People people;

		public String getAberHallo() {
			return aberHallo;
		}

		public void setAberHallo(String aberHallo) {
			this.aberHallo = aberHallo;
		}

		public int getAberSo() {
			return aberSo;
		}

		public void setAberSo(int aberSo) {
			this.aberSo = aberSo;
		}
	}

	public static class People {

		private String name;
		public int age;
		private Date dob;
		public boolean alive = false;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public Date getDob() {
			return dob;
		}

		public void setDob(Date dob) {
			this.dob = dob;
		}

		public People(String name, int age) {
			this.name = name;
			this.age = age;
		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws MalformedURLException, IOException, TemplateException, XMLException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, URISyntaxException {
		// Some business logic here. . . .
		if (1 == 2) {
			// Response to user
			// Find and instantiate template, located in the record directory
			// under our web application root.
			Template template = new Template("", "http://localhost:8080/lk_template/first.html");
			TheClass theClass = new TheClass();
			List peoples = new ArrayList();
			peoples.add(new People("can", 40));
			peoples.add(new People("imane", 27));
			peoples.add(new People("orhan", 41));

			theClass.aberHallo = "<ABERHALLO>";
			theClass.aberSo = 7;
			theClass.people = new People("orhan", 41);

			// Initialize some variables to be used by the template
			Map dictionary = new HashMap();
			dictionary.put("theClass", theClass);
			dictionary.put("people", peoples);
			dictionary.put("date", new Date());

			// Output response
			OutputStream output = System.out;
			//response.setContentType("text/html");
			template.setDictionary(dictionary);
			template.process();
			System.out.println(template.getContent());

			System.out.println(template.getXML());
			output.close();
		} else {
			/*
			 * the new template to test
			 */
			Template template = new Template("", "http://sntrk.org/aboutus.html");
			template.process();
			System.out.println(template.getContent());
		}
	}
}
