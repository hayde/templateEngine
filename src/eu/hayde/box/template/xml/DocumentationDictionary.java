/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.hayde.box.template.xml;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * this class will be generated for to make the documentation of a single
 * template page. so, that the html coder knows which elements are part of this
 * template
 *
 * @author can.senturk
 */
public class DocumentationDictionary {

	private String referenceName;
	private String className;
	private String sampleValue;
	private String valueType;
	private boolean isObject;
	private boolean isArray;
	private List<DocumentationDictionary> elements;

	public DocumentationDictionary(String referenceName, Object obj) throws XMLException {
		this.referenceName = referenceName;
		if (obj != null) {
			this.className = obj.getClass().getName();
			this.valueType = obj.getClass().getSimpleName();
			isObject = !_isAtom(obj);
			isArray = _isArrayOrList(obj);
		} else {
			this.className = "null";
			this.valueType = "unknown";
			this.isObject = false;
			this.isArray = false;
		}
		elements = new ArrayList<DocumentationDictionary>();

		if (!isObject) {
			// it is an atomic value, so we simply put the value in
			if (obj instanceof java.util.Date) {
				this.sampleValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) obj);

			} else if (obj instanceof XMLGregorianCalendar) {
				this.sampleValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS").format(((XMLGregorianCalendar) obj).toGregorianCalendar().getTime());

			} else {
				this.sampleValue = "" + obj;
			}

		} else if (obj instanceof Map) {


			Map map = (Map) obj;
			for (Object key : map.keySet()) {
				elements.add(new DocumentationDictionary("" + key, map.get(key)));
			}
		} else if (obj.getClass().isArray() || (obj instanceof List)) {
			List list = null;
			if (obj.getClass().isArray()) {
				list = new ArrayList(Arrays.asList(obj));
			} else if (obj instanceof List) {
				list = (List) obj;
			}

			for (int i = 0; i < list.size(); i++) {
				elements.add(new DocumentationDictionary(list.get(i).getClass().getSimpleName(), list.get(i)));
			}
		} else {
			// this map is required, because we will be able to prevent double
			// entries for the method and field calls
			Map<String, Object> getters = new HashMap<String, Object>();

			// loop for the public fields
			Field f[] = obj.getClass().getFields();
			for (int i = 0; i < f.length; i++) {
				Field currentField = f[i];
				currentField.setAccessible(true);
				try {
					getters.put(currentField.getName(), currentField.get(obj));
				} catch (IllegalArgumentException ex) {
					throw new XMLException("Value creation for '" + this.referenceName + "." + currentField.getName() + " failed: " + ex.getMessage());
				} catch (IllegalAccessException ex) {
					throw new XMLException("Value creation for '" + this.referenceName + "." + currentField.getName() + " failed: " + ex.getMessage());
				}
			}

			Method m[] = obj.getClass().getMethods();
			for (int i = 0; i < m.length; i++) {
				Method currentMethod = m[i];
				Object returnValue = null;
				String methodName = currentMethod.getName();


				// check, if this method is parameterless
				Class parameters[] = currentMethod.getParameterTypes();
				if (parameters.length == 0) {
					// yes, no parameter
					if (methodName.equals("getClass")) {
						// don't use that
					} else if (methodName.substring(0, 3).equals("get")
							|| methodName.substring(0, 2).equals("is")) {

						try {
							returnValue = currentMethod.invoke(obj);
						} catch (IllegalAccessException ex) {
							throw new XMLException("Value creation for '" + this.referenceName + "." + methodName + "() failed: method not reachable:" + ex.getMessage());
						} catch (IllegalArgumentException ex) {
							throw new XMLException("Value creation for '" + this.referenceName + "." + methodName + "() failed: method not reachable:" + ex.getMessage());
						} catch (InvocationTargetException ex) {
							throw new XMLException("Value creation for '" + this.referenceName + "." + methodName + "() failed: method not reachable:" + ex.getMessage());
						}

						// if the getter returned nothing ....
						if (returnValue == null) {
							try {
								// craete a new instance of the return value type
								returnValue = currentMethod.getReturnType().newInstance();
							} catch (InstantiationException ex) {
								/*
								 * this error occures, if the object doesn't
								 * have a null value constructor.
								 * like java.lang.Integer, ....
								 */
								//throw new XMLException("Value creation for '" + this.referenceName + "." + methodName + "() failed: couldn't initialize instance!" + ex.getMessage());
								returnValue = null;
							} catch (IllegalAccessException ex) {
								throw new XMLException("Value creation for '" + this.referenceName + "." + methodName + "() failed: Couldn't access method:" + ex.getMessage());
							}
						}
						// is boolean stuff
						getters.put(_renameGetterToField(methodName), returnValue);

					}
				}
			}

			// and now, put all the elements of the map into the dictionary
			for (String key : getters.keySet()) {
				DocumentationDictionary dDicttionary = new DocumentationDictionary(key, getters.get(key));
				elements.add(dDicttionary);
			}

		}
	}

	public String toJSON() {
		return _toJSON(0);
	}

	private String _toJSON(int recursionLevel) {
		recursionLevel++;
		StringBuilder returnValue = new StringBuilder();

		if (recursionLevel == 1) {
			returnValue.append("{\n");
			// ignore the dictionary root element!
			for (int i = 0, size = this.elements.size() - 1; i <= size; i++) {
				returnValue.append(this.elements.get(i)._toJSON(recursionLevel));
				if (i != size) {
					returnValue.append(",\n");
				}
			}
			returnValue.append("\n}");
		} else {

			returnValue.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t".substring(0, recursionLevel - 1));
			returnValue.append("\"");
			returnValue.append(this.referenceName);
			returnValue.append("\" : ");

			if (this.isObject) {
				if (this.isArray) {
					returnValue.append("[\n");
				} else {
					returnValue.append("{\n");
				}
				for (int i = 0, size = this.elements.size() - 1; i <= size; i++) {
					returnValue.append(this.elements.get(i)._toJSON(recursionLevel));
					if (i != size) {
						returnValue.append(",\n");
					}
				}
				returnValue.append("\n");
				returnValue.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t".substring(0, recursionLevel - 1));
				if (this.isArray) {
					returnValue.append("]");
				} else {
					returnValue.append("}");
				}
			} else {
				returnValue.append("\"");
				returnValue.append(this.sampleValue);
				returnValue.append("\"");
			}

		}
		recursionLevel--;
		return returnValue.toString().replaceAll("\t", "    ");
	}

	public String toXML() {
		StringBuilder returnValue = new StringBuilder();
		returnValue.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		returnValue.append(_toXML(0));
		return returnValue.toString().replaceAll(
				"\t", "    ");
	}

	private String _toXML(int recursionLevel) {
		StringBuilder returnValue = new StringBuilder();

		recursionLevel++;
		returnValue.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t".substring(0, recursionLevel));
		returnValue.append("<");
		returnValue.append(this.referenceName);
		returnValue.append(" javaObject='");
		returnValue.append(this.className);
		returnValue.append("'");

		if (!this.isObject) {
//			returnValue.append(" type='");
//
//			returnValue.append(this.valueType);
			returnValue.append(">");
			returnValue.append(this.sampleValue);

		} else {
			returnValue.append(" elementCount='");
			returnValue.append(this.elements.size());
			returnValue.append("'>\n");

			for (int i = 0; i < this.elements.size(); i++) {
				returnValue.append(this.elements.get(i)._toXML(recursionLevel));
			}
			returnValue.append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t".substring(0, recursionLevel));

		}
		//close tag
		returnValue.append("</");
		returnValue.append(this.referenceName);
		returnValue.append(">\n");
		recursionLevel--;


		return returnValue.toString();
	}

	private boolean _isAtom(Object obj) {

		boolean returnValue = false;
		Class classType = obj.getClass();


		if (classType.equals(java.lang.Long.class)
				|| classType.equals(java.lang.String.class)
				|| classType.equals(java.lang.Float.class)
				|| classType.equals(java.lang.Integer.class)
				|| classType.equals(java.lang.Boolean.class)
				|| classType.equals(java.util.Date.class)
				|| classType.equals(java.sql.Date.class)
				|| classType.equals(java.sql.Timestamp.class)
				|| classType.equals(java.sql.Time.class)
				|| classType.equals(java.lang.Double.class)
				|| classType.equals(java.math.BigDecimal.class)
				|| classType.equals(Byte.class)
				|| classType.equals(byte[].class)
				|| obj instanceof javax.xml.datatype.XMLGregorianCalendar
				|| obj.getClass().isEnum()) {
			returnValue = true;
		}

		return returnValue;

	}

	private boolean _isArrayOrList(Object obj) {

		boolean returnValue = false;

		if (obj.getClass().isArray()
				|| obj instanceof List) {
			returnValue = true;
		}

		return returnValue;

	}

	private String _renameGetterToField(String getterName) {
		String returnValue = getterName;
		if (getterName.startsWith("get")) {
			returnValue = getterName.substring(3);
		} else if (getterName.startsWith("is")) {
			returnValue = getterName.substring(2);
		}
		if (returnValue.equals("ID")) {
			// do not change that
		} else {
			// but others, ... you should
			returnValue = returnValue.substring(0, 1).toLowerCase()
					+ returnValue.substring(1);
		}
		return returnValue;
	}
}
