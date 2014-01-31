/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.hayde.box.template;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.NameSpace;
import eu.hayde.box.template.xml.XMLTagger;
import eu.hayde.box.template.xml.XMLException;
import eu.hayde.box.template.xml.Element;
import eu.hayde.box.template.xml.Attribute;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import eu.hayde.box.template.xml.Command;
import eu.hayde.box.template.xml.DocumentationDictionary;
import eu.hayde.box.template.converters.StringConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * this is a complete HTML template engine for java. it is specially designed to
 * help programmers to communicate with html coders and to help html coders to
 * left to right html code replacing system, that does have a good variable
 * definition and a standalone testing environment.
 *
 * @author can.senturk
 */
public class Template {

	private final static String HAYDE_NAMESPACEPREFIX = "hyd";
	private final static String HAYDE_INCLUDE = "include";
	private final static String HAYDE_CONTENT = "content";
	private final static String HAYDE_REPLACE = "replace";
	private final static String HAYDE_REPEAT = "repeat";
	private final static String HAYDE_CONDITION = "condition";
	private final static String HAYDE_COMMAND = "command";
	private final static String HAYDE_ATTRIBUTES = "attributes";
	private final static String HAYDE_REMOVETAG = "removeTag";
	private final static String HAYDE_TRANSLATE = "translate";
	private String baseDir;
	private String fileName;
	private String rawContent;
	private boolean recursionFlag;
	private String language;
	private Properties languageFile;
	private boolean languageChanged;
	private String languageFilename;
	private XMLTagger tagger;
	private Map<String, Object> dictionary = new HashMap<String, Object>();
	private boolean preprocessed = false;
	private Interpreter interpreter = new Interpreter();

	/**
	 * initializes the template engine.<br/>
	 * <br/>
	 * (translation enabled)
	 *
	 * @param baseDir the base directory. If it is empty of null, it will strip
	 * the baseDir from the filename. <br/>
	 * each request for a folder below this folder or on another device will be
	 * rejected. this is to disable hacking attacks from the template
	 * engine.<br>
	 * <br/>
	 * Important: if you have a multilingual template, this folder will contain
	 * the translation files for your system.
	 * @param templateFileName the filename of the template, which can contain
	 * some further folder information too.
	 */
	public Template(String baseDir, String templateFileName) {
		this.baseDir = baseDir;
		this.fileName = templateFileName;
		this.language = "en";
		this.recursionFlag = false;
		if (baseDir == null
				|| "".equals(baseDir)) {
			_stripBaseDirFromFile();
		}
	}

	/**
	 * instead of using a file template, you can initialize the template engine
	 * with a given content.<br/>
	 * <br/>
	 * (translation disabled)
	 * <br/>
	 * Important: if you use this initialization, you will not be able to
	 * translate a template, since a base folder name is missing.
	 *
	 * @param content
	 */
	public Template(String content) {
		this.rawContent = content;
		this.language = null;
		this.recursionFlag = false;
	}

	/**
	 * does initialize a template engine with a given content and a given
	 * interpreter.<br/>
	 * this is in general for internal use only.
	 *
	 * @param content
	 * @param interpreter
	 */
	public Template(String content, Interpreter interpreter, Properties languageFile) {
		this.interpreter.setNameSpace(new NameSpace(interpreter.getNameSpace(), "childRecursion"));
		this.rawContent = content;
		this.language = null;
		this.languageFile = languageFile;
		this.recursionFlag = true;
	}

	/**
	 * set the base directory. this will enable or disable the translation, if
	 * the value of the base directory is valid.<br/>
	 * <br/>
	 * (translation enabled)
	 *
	 * @param baseDir
	 */
	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
		if (this.baseDir != null
				&& this.baseDir.length() > 0
				&& this.language == null) {
			this.language = "en";
		}
	}

	/**
	 * does set the language code, if there is a valid baseDir
	 *
	 * @param languageCode
	 */
	public void setLanguage(String languageCode) {
		if (languageCode == null) {
			// disable the translation function
			this.language = null;
		} else if (this.baseDir != null && languageCode.length() == 2) {
			this.language = languageCode;
		}
	}

	/**
	 * return the curren valid language Code
	 *
	 * @return String, default = "en"
	 */
	public String getLanguage() {
		return this.language;
	}

	public void addObject(String key, Object Object) {
		dictionary.put(key, Object);
	}

	public void setDictionary(Map dictionary) {
		this.dictionary = dictionary;
	}

	private void _preprocess() throws XMLException, TemplateException {
		if (this.rawContent == null) {
			if (this.fileName != null) {
				this.rawContent = _readURL(this.baseDir, this.fileName);
			}
		}
		tagger = new XMLTagger(rawContent);
		// set all interpreter values
		for (String key : dictionary.keySet()) {
			try {
				interpreter.set(key, dictionary.get(key));
			} catch (EvalError ex) {
				throw new XMLException("Was not able to set value '" + key + "' to interpreter.");
			}
		}

		// now add the functions!
		try {
			interpreter.set("String", new StringConverter(this.language));
		} catch (EvalError ex) {
			throw new XMLException("Was not able to set functions to interpreter.");
		}

		// load the translation files
		if (this.language != null
				&& this.language.length() > 0
				&& this.recursionFlag == false) {

			// load the language file
			_loadLanguageFile();
		}
		preprocessed = true;
	}

	private void _postProcess() throws TemplateException {
		if (this.languageChanged) {
			_saveLanguageFile();
		}
	}

	public String getXML() throws XMLException {
		DocumentationDictionary dd = new DocumentationDictionary("Dictionary", dictionary);

		return dd.toXML();
	}

	public String getJSON() throws XMLException {
		DocumentationDictionary dd = new DocumentationDictionary("Dictionary", dictionary);

		return dd.toJSON();
	}

	public int process() throws TemplateException, XMLException {
		int actionCount = 0;
		if (!preprocessed) {
			_preprocess();
		}

		String nextCommand = tagger.getNextTag(HAYDE_NAMESPACEPREFIX);

		while (nextCommand != null) {
			if (nextCommand.equalsIgnoreCase(HAYDE_INCLUDE)) {
				actionCount += _processIncludes();

			} else if (nextCommand.equalsIgnoreCase(HAYDE_TRANSLATE)) {
				actionCount += _processTranslate();

			} else if (nextCommand.equalsIgnoreCase(HAYDE_CONTENT)) {
				actionCount += _processContent();

			} else if (nextCommand.equalsIgnoreCase(HAYDE_REPLACE)) {
				actionCount += _processReplace();

			} else if (nextCommand.equalsIgnoreCase(HAYDE_REPEAT)) {
				actionCount += _processRepeat();

			} else if (nextCommand.equalsIgnoreCase(HAYDE_CONDITION)) {
				actionCount += _processCondition();

			} else if (nextCommand.equalsIgnoreCase(HAYDE_COMMAND)) {
				actionCount += _processCommand();

			} else if (nextCommand.equalsIgnoreCase(HAYDE_ATTRIBUTES)) {
				actionCount += _processAttributes();

			} else if (nextCommand.equalsIgnoreCase(HAYDE_REMOVETAG)) {

				actionCount += _processRemoveTag();
			} else {
				throw new XMLException("Unknown hyd attribute or command: 'hyd:" + nextCommand + "'!");
			}

			/*
			 * get the next command
			 */
			nextCommand = tagger.getNextTag(HAYDE_NAMESPACEPREFIX);
		}

		_postProcess();

		return actionCount;
	}

	public String getContent() {
		return this.tagger.toString();
	}

	private int _processIncludes() throws XMLException, TemplateException {
		int actionCount = 0;
		String attributeName = HAYDE_NAMESPACEPREFIX + ":" + HAYDE_INCLUDE;
		Element element = tagger.getByAttribute(attributeName);
		if (element != null) {

			actionCount++;
			Attribute attribute = element.getAttribute(attributeName);


			/*
			 * if the url is set, we will load from a different server
			 */
			String includeContent;

			includeContent = _readURL(this.baseDir, attribute.value);

			tagger.replace(element, includeContent);

		}
		return actionCount;
	}

	private int _processRemoveTag() throws XMLException {
		int actionCount = 0;
		String attributeName = HAYDE_NAMESPACEPREFIX + ":" + HAYDE_REMOVETAG;
		Element element = tagger.getByAttribute(attributeName);
		String errorMsgTag = null;

		if (element != null) {
			actionCount++;

			/*
			 * remember the tag for further error messages
			 */
			errorMsgTag = element.getStartTag().tag;

			/*
			 * get the attribute and remove the attribute from the tag it.
			 */
			Attribute attribute = element.getAttribute(attributeName);
			tagger.removeAttribute(element, attribute);

			/* now check, if there are furhter hyd:xxx tags in the current
			 * tag. if yes, throw an error, because the remove tag
			 * has to be the right most attribute in a tag
			 */
			if (element.getStartTag().tag.indexOf(HAYDE_NAMESPACEPREFIX + ":") > -1) {
				throw new XMLException("hyd:removeTag has to be the right most attribute in the tag: " + element.getStartTag().tag);
			}

			/*
			 * finally we can simply remove the complete tag, that has to be
			 * removed
			 */
			tagger.removeTag(element);

		}
		return actionCount;
	}

	private int _processContent() throws XMLException {
		int actionCount = 0;
		String attributeName = HAYDE_NAMESPACEPREFIX + ":" + HAYDE_CONTENT;
		Element element = tagger.getByAttribute(attributeName);
		if (element != null) {
			actionCount++;
			Attribute attribute = element.getAttribute(attributeName);

			String output = _parseCode(attribute.value);

			tagger.setContent(element, output);
			tagger.removeAttribute(element, attribute);
			// check for next item
			//element = tagger.getByAttribute(attributeName);
		}
		return actionCount;
	}

	private int _processTranslate() throws XMLException, TemplateException {
		int actionCount = 0;
		String attributeName = HAYDE_NAMESPACEPREFIX + ":" + HAYDE_TRANSLATE;
		Element element = tagger.getByAttribute(attributeName);
		if (element != null) {
			actionCount++;
			Attribute attribute = element.getAttribute(attributeName);

			String output = _translate(attribute.value, tagger.getContent(element));

			tagger.setContent(element, output);
			tagger.removeAttribute(element, attribute);
			// check for next item
			//element = tagger.getByAttribute(attributeName);
		}
		return actionCount;
	}

	private int _processAttributes() throws XMLException, TemplateException {
		int actionCount = 0;
		String attributeName = HAYDE_NAMESPACEPREFIX + ":" + HAYDE_ATTRIBUTES;
		Element element = tagger.getByAttribute(attributeName);
		if (element != null) {
			actionCount++;
			Attribute attribute = element.getAttribute(attributeName);

			// repeat for every attribute requst
			if (!attribute.value.contains(";")) {
				// single attribute
				Attribute newAttribute = _parseAttribute(attribute.value);

				tagger.addAttribute(element, newAttribute);
			} else {
				String[] attributes = attribute.value.split(";");
				for (int i = 0; i < attributes.length; i++) {
					Attribute newAttribute = _parseAttribute(attributes[i]);

					/*
					 * if a attribute has the suffix "[noval]", then it should be
					 * hidden, if empty
					 */
					if (newAttribute.name.endsWith("[noval]")
							&& (newAttribute.value == null
							|| newAttribute.value.equals(""))
							|| newAttribute.value.equals("false")) {
						/*
						 * no value, so don't add this attribute
						 */
					} else {
						tagger.addAttribute(element, newAttribute);
					}

				}
			}

			tagger.removeAttribute(element, attribute);
			// check for next item
			//element = tagger.getByAttribute(attributeName);
		}
		return actionCount;
	}

	private int _processReplace() throws XMLException, TemplateException {
		int actionCount = 0;
		String attributeName = HAYDE_NAMESPACEPREFIX + ":" + HAYDE_REPLACE;
		Element element = tagger.getByAttribute(attributeName);
		if (element != null) {
			actionCount++;
			Attribute attribute = element.getAttribute(attributeName);

			String output = _parseCode(attribute.value);

			tagger.replace(element, output);

			// check for next item
			//element = tagger.getByAttribute(attributeName);
		}
		return actionCount;
	}

	private int _processCommand() throws XMLException, TemplateException {
		int actionCount = 0;
		String attributeName = HAYDE_NAMESPACEPREFIX + ":" + HAYDE_COMMAND;
		Element element = tagger.getByAttribute(attributeName);
		if (element != null) {
			actionCount++;
			Attribute attribute = element.getAttribute(attributeName);

			String output = _parseCode(attribute.value);

			tagger.removeAttribute(element, attribute);
			//tagger.replace(element, "");

			// check for next item
			//element = tagger.getByAttribute(attributeName);
		}
		return actionCount;
	}

	private int _processCondition() throws XMLException, TemplateException {
		int actionCount = 0;
		String attributeName = HAYDE_NAMESPACEPREFIX + ":" + HAYDE_CONDITION;
		Element element = tagger.getByAttribute(attributeName);
		if (element != null) {
			actionCount++;

			Attribute attribute = element.getAttribute(attributeName);
			if (_parseCondition(attribute.value)) {
				// ok, leave this, just remove the attribute
				tagger.removeAttribute(element, attribute);

			} else {
				// delete this complete tag
				tagger.replace(element, "");
			}

			// check for next item
			//element = tagger.getByAttribute(attributeName);
		}
		return actionCount;
	}

	private int _processRepeat() throws XMLException, TemplateException {
		int actionCount = 0;
		String attributeName = HAYDE_NAMESPACEPREFIX + ":" + HAYDE_REPEAT;
		Element element = tagger.getByAttribute(attributeName);
		if (element != null) {
			actionCount++;
			Attribute attribute = element.getAttribute(attributeName);

			// remove the attribute before process that code, because
			// it will be copied otherwise to every block!
			tagger.removeAttribute(element, attribute);

			String output = _parseRepeat(attribute, element);

			tagger.replace(element, output);

			// check for next item
			//element = tagger.getByAttribute(attributeName);
		}
		return actionCount;
	}

	private String _readFile(File theFileToRead) throws TemplateException {
		String content = null;

		try {
			FileReader reader = new FileReader(theFileToRead);
			char[] chars = new char[(int) theFileToRead.length()];
			reader.read(chars);
			content = new String(chars);
			reader.close();
		} catch (IOException ex) {
			throw new TemplateException("Unable to load template of File '" + theFileToRead.getAbsolutePath() + "'!");
		}
		return content;
	}

	private String _readURL(String directory, String filename) throws TemplateException {
		StringBuilder sb = new StringBuilder();
		URLConnection urlConn;
		InputStreamReader in = null;
		URL url = _getURL(directory, filename);

		/*
		 * check, if the basedir is still member of this url
		 */
		if (url == null) {
			throw new TemplateException("template is not in the base root. Denied loading template '" + filename + "'!");
		}
		try {
			urlConn = url.openConnection();
			if (urlConn != null) {
				urlConn.setReadTimeout(60 * 1000);
			}
			if (urlConn != null && urlConn.getInputStream() != null) {
				in = new InputStreamReader(urlConn.getInputStream(),
						Charset.defaultCharset());
				BufferedReader bufferedReader = new BufferedReader(in);
				int cp;
				while ((cp = bufferedReader.read()) != -1) {
					sb.append((char) cp);
				}
				bufferedReader.close();
			}
			if (in != null) {
				in.close();
			}
		} catch (IOException ex) {
			throw new TemplateException("Unable to load template of URL '" + filename + "'!");
		}

		return sb.toString();
	}

	private String _parseCode(String parCode) throws XMLException {
		// code could be like: class.variablename
		Command code = new Command(interpreter, dictionary, languageFile, parCode);

		return "" + code.run();
	}

	private String _parseRepeat(Attribute attribute, Element element) throws XMLException, TemplateException {
		// code could be like: class.variablename
		Command code = new Command(interpreter, dictionary, languageFile, attribute.value);

		return code.repeat(tagger.getTag(element), this.baseDir);
	}

	private Attribute _parseAttribute(String parCode) throws TemplateException, XMLException {
		if (!parCode.contains(":")) {
			throw new TemplateException("Error in hyd:attribute while parsing code '" + parCode + "'. Declaration to which attribute is missing like 'href : people.name'");
		}
		Attribute returnValue = new Attribute();
		returnValue.name = parCode.substring(0, parCode.indexOf(":")).trim();
		returnValue.value = parCode.substring(parCode.indexOf(":") + 1).trim();
		Command code = new Command(interpreter, dictionary, languageFile, returnValue.value);

		returnValue.value = "" + code.run();
		return returnValue;
	}

	private boolean _parseCondition(String parCode) throws XMLException {
		Command code = new Command(interpreter, dictionary, languageFile, parCode);
		boolean returnValue = false;
		Object result = code.run();
		if (result instanceof Boolean) {
			returnValue = (Boolean) result;
		} else if (result instanceof Integer) {
			returnValue = ((Integer) result != 0) ? true : false;
		} else if (result instanceof String) {
			returnValue = ("true".equals((String) result)) ? true : false;
		}

		return returnValue;
	}

	/**
	 * this function checkes, if the given file in combination with the
	 * directory is a hack or not.<br/>
	 * here, it will stop stuff like: "D:/rootDirOfTemplate/" +
	 * "../anotherRoot/badFile.txt"<br/>
	 *
	 * @param directory
	 * @param filename
	 * @return
	 * @throws TemplateException
	 */
	private URL _getURL(String directory, String filename) throws TemplateException {
		boolean matchesToBase = false;
		URL baseURL = null;
		URL fileURL = null;
		int marker = 0;

		try {
			marker = 1;	// before baseURL
			if (directory != null
					&& !"".equals(directory)) {
				baseURL = new URL(directory);
			}
			marker = 2; // at file URL
			fileURL = new URL(directory + "/" + filename);

			/*
			 * if there is no base directory, return true
			 */
			if (baseURL == null) {
				matchesToBase = true;
			} else {
				marker = 3; // before if
				if (fileURL.toURI().normalize().toString().startsWith(
						baseURL.toURI().normalize().toString())) {
					matchesToBase = true;
				}
			}
		} catch (URISyntaxException ex) {
			throw new TemplateException("Not able to convert URL to URI for file '" + filename + "'.");
		} catch (MalformedURLException ex) {
			if (marker == 1) {
				throw new TemplateException("Malformed Base URL for file '" + directory + "'.");
			} else if (marker == 2) {
				throw new TemplateException("Malformed URL for file '" + filename + "'.");
			} else if (marker == 3) {
				throw new TemplateException("URI Exception for file '" + filename + "'.");
			}
		}
		if (matchesToBase) {
			return fileURL;
		} else {
			return null;
		}
	}

	private void _stripBaseDirFromFile() {
		this.baseDir = this.fileName;
		this.fileName = new File(this.fileName).getName();
		this.baseDir = this.baseDir.substring(0, this.baseDir.length() - this.fileName.length());
	}

	/**
	 * does load from a property file the value for the given key. if there is
	 * no value for this key, it will give back the default value and create a
	 * new entry to the property file, to translate.
	 *
	 * @param key
	 * @param defaultValue
	 * @return String the value for the key, stored in the language property
	 * file (according to the language setting) or the default value.
	 */
	private String _translate(String key, String defaultValue) throws TemplateException {
		String returnValue = null;

		if (languageFile == null) {
			// if there is no language to translate, just replace it with the
			// default
			returnValue = defaultValue;

		} else {

			if (languageFile.containsKey(key)) {
				returnValue = languageFile.getProperty(key);
			} else {
				returnValue = defaultValue;
				this.languageFile.setProperty(key, defaultValue);
				this.languageChanged = true;

			}
		}

		return returnValue;
	}

	private void _loadLanguageFile() throws TemplateException {
		if (languageFile == null) {
			languageFile = new Properties() {
				@Override
				public synchronized Enumeration<Object> keys() {
					return Collections.enumeration(new TreeSet<Object>(super.keySet()));
				}
			};

			this.languageChanged = false;

			InputStream is;
			languageFilename = this.baseDir + "translate." + this.language;
			try {
				File inputFile = __urlToFile(new URL(languageFilename));
				is = new FileInputStream(inputFile);
				this.languageFile.load(is);
				is.close();
			} catch (FileNotFoundException ex) {
				// if there is no file, we will store it
				this.languageChanged = true;
			} catch (IOException ex) {
				Logger.getLogger(Template.class.getName()).log(Level.SEVERE, "loading translation file: " + languageFilename, ex);
				throw new TemplateException("error loading translation file " + languageFilename);
			}
		}
	}

	private void _saveLanguageFile() throws TemplateException {
		if (languageFile != null
				&& this.recursionFlag == false) {
			OutputStream os;
			try {
				File yourFile = __urlToFile(new URL(languageFilename));
				if (!yourFile.exists()) {
					yourFile.createNewFile();
				}
				os = new FileOutputStream(yourFile, false);
				this.languageFile.store(os, "haydeTemplate language file");
				os.close();
			} catch (FileNotFoundException ex) {
				Logger.getLogger(Template.class.getName()).log(Level.SEVERE, "file not found while storing translation file: " + languageFilename, ex);
				throw new TemplateException("file not found while storing translation file " + languageFilename);
			} catch (IOException ex) {
				Logger.getLogger(Template.class.getName()).log(Level.SEVERE, null, ex);
				throw new TemplateException("error storing translation file " + languageFilename);
			}

		}
	}

	private File __urlToFile(URL url) {
		File returnValue;
		returnValue = new File(url.getFile());
		return returnValue;
	}
}
