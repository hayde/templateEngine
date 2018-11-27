/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.hayde.box.template.xml;

import java.util.ArrayList;
import java.util.List;
import eu.hayde.box.template.Template;
import eu.hayde.box.template.TemplateException;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 *
 * @author can.senturk
 */
public class Command {

	public String object;
	public String variable;
	public String declaration; // for loops (repeats)
	public String encoding;
	public String condition;
	public String codeString;
	public ScriptEngine interpreter;
	public Properties languageFile;

	public Command(ScriptEngine interpreter, Properties languageFile, String code) {
		this.interpreter = interpreter;
		this.languageFile = languageFile;
		Pattern declarationPattern = Pattern.compile("^\\s*([a-zA-Z0-9_]+)\\s*\\:");
		Matcher declarationMatcher = declarationPattern.matcher(code);

		// repeat??
		if (declarationMatcher.find()) {
			// repeating declaration is here

			declaration = declarationMatcher.group(1);
			code = code.substring(declarationMatcher.group(0).length()).trim();

		}

		codeString = code;
		//codeString = codeString.replaceAll("\'", "\"").trim();	// the interpreter doesn't allow single quotes

		// object and varible
		if (!code.contains(".")) {
			// that is the key
			object = code.trim();
		} else {
			String[] variableSplit = code.split("\\.");
			if (variableSplit.length > 1) {
				object = variableSplit[0].trim();
				variable = code.substring(code.indexOf(".") + 1).trim();
			}
		}

	}

	public Object run() throws XMLException {
		// check, if the code string is a simple variable check.
//		if (codeString.matches("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)*")) {
//			return _getDictionaryValue(codeString);
//		} else {
		try {
			return interpreter.eval(codeString);
		} catch (ScriptException ex) {
			throw new XMLException("Unable to process code of '" + codeString + "': " + ex.getMessage());
		}
//		}

	}

	public String repeat(String block, String baseDir) throws XMLException, TemplateException {

		// check, if the element is existing
		Object obj = run();
		StringBuilder returnValue = new StringBuilder();
		//String returnValue = "";

		// check if null
		if (obj == null) {
			// no element, so return nothing
		} else {

			List elements = null;

			if (obj.getClass().isArray()) {
				Object[] objectArray = (Object[]) obj;
				elements = new ArrayList();
				for (int i = 0; i < objectArray.length; i++) {
					elements.add(objectArray[i]);
				}
			} else if (obj instanceof List) {
				elements = (List) obj;
			} else if (obj instanceof Integer) {
				int elementCount = (Integer) obj;
				elements = new ArrayList<Integer>();
				for (int i = 1; i <= elementCount; i++) {
					elements.add(new Integer(i));
				}
			}

			if (elements == null) {
				throw new XMLException("Expression '" + codeString + "' doesn't return a Array or a List! Invalid value :" + elements);
			} else {
				// so, now for every entry ...
				for (int i = 0; i < elements.size(); i++) {
					Object singleItem = elements.get(i);
					Template tmpTemplate = new Template(block, interpreter, languageFile);
					tmpTemplate.setBaseDir(baseDir);
					// but disable the translation, because the parent task
					// will handle it
					tmpTemplate.setLanguage(null);
					tmpTemplate.addObject(declaration, singleItem);
					tmpTemplate.addObject("counter", i);
					tmpTemplate.process();
					returnValue.append( tmpTemplate.getContent() );
				}
			}

		}
		return returnValue.toString();
	}
}
