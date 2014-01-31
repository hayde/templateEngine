/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import bsh.EvalError;
import bsh.Interpreter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import eu.hayde.box.template.TemplateException;
import eu.hayde.box.template.xml.XMLException;
import java.net.URLEncoder;

/**
 *
 * @author senturk
 */
public class BashSamples {

	public static class TheClass {

		public String aberHallo;
		public int aberSo;
		public templateTester.People people;

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

	public static void main(String[] args) throws MalformedURLException, IOException, TemplateException, XMLException, EvalError, URISyntaxException {
		Interpreter i = new Interpreter();

		List peoples = new ArrayList();
		peoples.add(new templateTester.People("can", 40));
		peoples.add(new templateTester.People("imane", 27));
		peoples.add(new templateTester.People("orhan", 41));

		i.set("x", peoples);
		i.set("y", new templateTester.People("jjj", 23));

		Object returnValue = i.eval(
				"y.name");
		if (returnValue instanceof List) {
			System.out.println(" List ! ");
		}
		System.out.println(returnValue);

		System.out.println(URLEncoder.encode("random word £500 bank $", "UTF-8"));

		String[] x = new String[]{"iphone", "blackberry", "android", "nokia"};

		URL url = new URL("http://www.google.de/test/");
		URI uri = url.toURI();
		System.out.println(uri.resolve("second.html"));
		System.out.println(uri.resolve("../../../second.html"));
		System.out.println(uri.resolve("hallo/second.html?%20a%20+%20b%20=%20c"));
		System.out.println(uri.resolve("http://www.sonsostwo.de/"));


	}
}
