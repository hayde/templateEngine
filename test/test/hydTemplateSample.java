/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import eu.hayde.box.template.Template;
import eu.hayde.box.template.util.StringBufferExt;
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
//		Template template = new Template(_template_string(), new ScriptEngineManager().getEngineByName("nashorn") );
		for( int a = 0; a<1; a++ ) {
					System.out.println( a + " ------------" );

			long pure_start = System.nanoTime();
			Template template = new Template(_template_script_string());

			List<Person> persons = new ArrayList<Person>();

			template.addObject( "time", "now" );

			for( int i=0; i<3000; i++ ) {
				persons.add(new Person("Ahmet", "Hashim", new Profession("poet", 19)));
				persons.add(new Person("Sun", "Tzu", new Profession("mathematician", -5)));
				persons.add(new Person("Ṣalāḥ al-Dīn", "ibn Ayyūb", new Profession("sultan", 12)));
			}

			template.addObject("persons", persons);
			template.addObject("nongrada", persons);

			long start = System.nanoTime();
			int actions = template.process();
			long stop = System.nanoTime();

			String content = template.getContent();
			if( content.length() < 3000000 ) {
				System.out.println(template.getContent());
			} else {
				System.out.println(template.getContent().substring(0, 300) + " ...");
			}
			//System.out.println(template.getXML());
			//System.out.println(template.getJSON());

			System.out.println(" Actions >> " + actions);

			System.out.println( (stop - start)  );
			System.out.println( (stop - pure_start)  );
			
			System.out.println( "Counter: " + StringBufferExt.counter );
		}
	}
	
	public static String _template_string() {
		return "<html>\n" +
"<tag hyd:repeat=\"person : persons\">\n" +
"   <tag hyd:replace=\"StringConverter.toUpperCase(time)\"></tag>\n" +
"	<tag hyd:content=\"person.surname\" hyd:attributes=\"value:time;nochmal:time;oder:time\">s</tag>\n" +
"	<tag hyd:content=\"person.lastname\" hyd:attributes=\"value:time;nochmal:time;oder:time\">l</tag>\n" +
"	<tag hyd:content=\"person.profession.title\" hyd:attributes=\"value:time;nochmal:time;oder:time\">pt</tag>\n" +
"</tag>\n" +
"</html>";
		
	}
	
	public static String _template_script_string() {
		return "<html hyd:command=\"gomandoo_scripts='/Users/sntrk/'\">\n" +
"<tag hyd:content=\"load( gomandoo_scripts + 'template_test.js');\">\n" +
"</tag>\n" +
"</html>";
		
	}
}
