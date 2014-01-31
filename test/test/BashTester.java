package test;

import bsh.EvalError;
import bsh.Interpreter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author can.senturk
 */
public class BashTester {

	public static class NodeClass {

		public String name;
		public String value;
		public Map<String, NodeClass> children = new HashMap<String, NodeClass>();

		public NodeClass(String name) {
			this.name = name;
		}

		public NodeClass(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}

	public static void main(String[] args) throws EvalError {
		Interpreter interpreter = new Interpreter();
		interpreter.set("questionA", new String("answer"));
		interpreter.set("questionB", "answer");

		System.out.println(interpreter.eval("questionA==\"answer\"")); // -> here false
		System.out.println(interpreter.eval("questionB==\"answer\"")); // -> here true

		Map<String, Object> tester = new HashMap<String, Object>();
		tester.put("one", "valueof_one");
		tester.put("two", "valueof_two");

		interpreter.set("tester", tester);
		System.out.println(interpreter.eval("tester.one"));


		/*
		 * further tests
		 */
		NodeClass root = new NodeClass("root", "none");
		NodeClass first = new NodeClass("first", "valueof_first");
		NodeClass second = new NodeClass("second", "valueof_second");
		NodeClass firstOfFirst = new NodeClass("firstOfFirst", "valueof_firstOfFirst");
		NodeClass secondOfFirst = new NodeClass("secondOfFirst", "valueof_secondOfFirst");

		first.children.put("first", firstOfFirst);
		first.children.put("second", secondOfFirst);

		root.children.put("first", first);
		root.children.put("second", second);

		interpreter.set("map", root);

		System.out.println("map: " + interpreter.eval("map.children.first.children.first.name"));
		System.out.println("map: " + interpreter.eval("map.children.first.children.first.value"));
		System.out.println("map: " + interpreter.eval("map.children.first.name"));
		System.out.println("map: " + interpreter.eval("map.children.first.value"));
		System.out.println("map: " + interpreter.eval("map.name"));
		System.out.println("map: " + interpreter.eval("map.value"));


	}
}
