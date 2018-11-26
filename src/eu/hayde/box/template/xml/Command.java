/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.hayde.box.template.xml;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import eu.hayde.box.template.Template;
import eu.hayde.box.template.TemplateException;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.Bindings;
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
	private Map<String, Object> dictionary;
	private Bindings binding;

	public Command(ScriptEngine interpreter, Bindings binding, Map<String, Object> dictionary, Properties languageFile, String code) {
		this.interpreter = interpreter;
		this.binding = binding;
		this.dictionary = dictionary;
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
		codeString = codeString.replaceAll("\'", "\"").trim();	// the interpreter doesn't allow single quotes

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
			return interpreter.eval(codeString, binding);
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

	private Object _getDictionaryValue(String codeString) throws XMLException {
		String currentObject = "";
		String nextObject = "";
		Object returnValue = null;

		if (codeString.contains(".")) {
			currentObject = codeString.substring(0, codeString.indexOf("."));
			nextObject = codeString.substring(currentObject.length() + 1, codeString.length());
		} else {
			currentObject = codeString;
		}
		if (currentObject != null) {

			// it is a object in the structure
			Object obj = dictionary.get(currentObject);

			try {
				if (this.variable == null) {
					// then it is a final object, take it!
					returnValue = obj;
				} else {
					// return the value of the variable
					returnValue = _getObjectValue(obj, nextObject);
				}
			} catch (XMLException ex) {
				throw new XMLException("Error parsing code '" + currentObject + "." + nextObject + "':" + ex.getMessage());
			}

		}
		return returnValue;
	}

	private Object _getObjectValue(Object obj, String name) throws XMLException {
		Object returnValue = null;
		String fieldName = null;
		String nextName = null;

		if (name.contains(".")) {

			String[] variableSplit = name.split("\\.");
			if (variableSplit.length > 1) {
				fieldName = variableSplit[0];
				nextName = name.substring(fieldName.length() + 1);
			}

		} else {
			fieldName = name;
		}

		try {

			if (obj instanceof Map) {

				returnValue = ((Map) obj).get(fieldName);
			} else {
				/*
				 * it is a field element (so stored inside an object)
				 */

				Field currentField = null;
				try {
					currentField = obj.getClass().getField(fieldName);
				} catch (NoSuchFieldException ex) {
				}
				if (currentField != null) {
					// if the field is with public access, than take the field
					currentField.setAccessible(true);
					returnValue = currentField.get(obj);
				} else {
					// if not, we require a getter for that variable
					String methodName = _findValidMethodName(obj.getClass().getMethods(), fieldName);
					if (fieldName.equals("available")) {
						fieldName = fieldName;
					}
					Method getFunction = obj.getClass().getMethod(methodName);
					if (getFunction != null) {
						returnValue = getFunction.invoke(obj);
					}
				}
			}
		} catch (SecurityException ex) {
			throw new XMLException("Access denied for Field '" + name + "'.");
		} catch (IllegalArgumentException ex) {
			throw new XMLException("Argument exception for '" + name + "'.");
		} catch (IllegalAccessException ex) {
			throw new XMLException("Illegal access exception for Field '" + name + "'.");
		} catch (NoSuchMethodException ex) {
			throw new XMLException("variable '" + name + "' requires getter like : '" + _getGetterName(fieldName) + "()'");
		} catch (InvocationTargetException ex) {
			throw new XMLException("getter '" + _getGetterName(fieldName) + "' created an internal error: " + ex.getMessage());
		}

		if (nextName != null) {
			// now get the next object:
			returnValue = _getObjectValue(returnValue, nextName);
		}

		return returnValue;
	}

	private String _getUppercaseName(String field) {
		return field.substring(0, 1).toUpperCase() + field.substring(1);
	}

	private String _getGetterName(String field) {
		return "get" + _getUppercaseName(field);
	}

	private String _getBooleanName(String field) {
		return "is" + _getUppercaseName(field);
	}

	private String _findValidMethodName(Method[] methods, String fieldName) {
		String returnValue = null;
		if (methods != null) {

			String isName = this._getBooleanName(fieldName);
			String getterName = this._getGetterName(fieldName);

			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().equals(isName)) {
					returnValue = methods[i].getName();
					break;
				} else if (methods[i].getName().equalsIgnoreCase(getterName)) {
					returnValue = methods[i].getName();
					break;
				}
			}
		}
		return returnValue;
	}
}
