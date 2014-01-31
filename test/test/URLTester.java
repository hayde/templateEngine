/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 * @author senturk
 */
public class URLTester {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws MalformedURLException, URISyntaxException {
		// TODO code application logic here

		String baseDir = "file://localhost/Users/senturk/Downloads/";
		String file = "../likaliner.log.sql";

		URL url = new URL(baseDir + file);
		System.out.println(url.getAuthority());
		System.out.println(url.getFile());
		System.out.println(url.getHost());
		System.out.println(url.getPath());
		System.out.println(url.getPort());
		System.out.println(url.getProtocol());
		System.out.println(url.getQuery());
		System.out.println(url.getRef());
		System.out.println(url.getUserInfo());
		System.out.println(url.toExternalForm());

		URI uri = url.toURI();

		System.out.println(uri.getAuthority());
		System.out.println(uri.getFragment());
		System.out.println(uri.getHost());
		System.out.println(uri.getPath());
		System.out.println(uri.getPort());
		System.out.println(uri.getQuery());
		System.out.println(uri.getRawAuthority());
		System.out.println(uri.getRawFragment());
		System.out.println(uri.getRawPath());
		System.out.println(uri.getRawQuery());
		System.out.println(uri.getRawSchemeSpecificPart());
		System.out.println(uri.normalize().toString());


	}
}
