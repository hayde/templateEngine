/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.hayde.box.template.cms;

import eu.hayde.box.template.cms.infos.HTMLInfo;
import eu.hayde.box.template.cms.infos.FileInfo;
import eu.hayde.box.template.cms.infos.DirectoryInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ArmenNalbandyan
 */
public class CMSContainer {

	//private DirectoryInfo rootDirectoryInfo = null;
	private String rootDirectory = null;
	private String absolutePathPrefix = null;

	public CMSContainer(String parRoot) throws CMSInitializationError {
		rootDirectory = parRoot;
		rootDirectory = rootDirectory.replaceAll("\\\\", "/");

		if (!rootDirectory.endsWith("/")) {
			rootDirectory += "/";
		}
	}

	public void setAbsolutePath(String pathPrefix) {
		absolutePathPrefix = pathPrefix;
	}

	public String getAbsolutePath() {
		return absolutePathPrefix;
	}

	public HTMLInfo loadFile(String parFilename) throws IOException, CMSInitializationError {
		HTMLInfo returnValue = createHtmlInfo(parFilename);

		/*
		 * load index of this folder
		 */

		return returnValue;
	}

	private HTMLInfo createHtmlInfo(String parFilename) throws IOException, CMSInitializationError {

		if (rootDirectory == null) {
			throw new CMSInitializationError();
		}
		HTMLInfo fileInfo = new HTMLInfo();
		fileInfo.setAbsolutePath(this.absolutePathPrefix);
		File file;

		/*
		 * there is the posibility, that the filename contains the root directory
		 * already
		 */
		parFilename = parFilename.replaceAll("\\\\", "/");
		if (!parFilename.startsWith("/")) {
			parFilename = "/" + parFilename;
		}
		if (parFilename.startsWith(this.rootDirectory)) {
			file = new File(parFilename);
		} else {
			file = new File(this.rootDirectory + parFilename);
		}
		String fileContent = readFile(file);
		//if (withContent) {
		fileInfo.setRaw(fileContent);
		//}
		fileInfo.setFileName(file.getName().replaceFirst("[.][^.]+$", ""));
		fileInfo.setFileCreationDate(new Date(file.lastModified()));
		String fileAbsolutePath = file.getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
		if (fileAbsolutePath.charAt(0) != '/') {
			fileAbsolutePath = "/" + fileAbsolutePath;
		}
		if (fileAbsolutePath.startsWith(this.rootDirectory)) {
			fileInfo.setParentFolderName(fileAbsolutePath.substring(this.rootDirectory.length()));
		} else {
			fileInfo.setParentFolderName("");
		}
		return fileInfo;
	}

	/**
	 * does load the files of a given folder
	 *
	 * @param folderName
	 * @return
	 * @throws IOException
	 * @throws CMSInitializationError
	 */
	private DirectoryInfo loadFiles(String folderName) throws IOException, CMSInitializationError {
		if (rootDirectory == null) {
			throw new CMSInitializationError();
		}
		DirectoryInfo directoryInfo = new DirectoryInfo();
		directoryInfo.setFileName(rootDirectory + folderName);

		File rootFile = new File(rootDirectory + folderName);
		File[] fileList = rootFile.listFiles();
		for (int i = 0; i < fileList.length; ++i) {
			File currentFile = fileList[i];
			if (currentFile.isDirectory()) {
				/*
				 * not recursive from now on ...
				 */
//				directoryInfo.addFileInfo(loadFiles(currentFile.getCanonicalPath()));
			} else {
				String filename = currentFile.getName();
				String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
				if ("html".equals(extension) || "htm".equals(extension)) {
					directoryInfo.addFileInfo(createHtmlInfo(currentFile.getAbsolutePath()));
				}
			}
		}
		return directoryInfo;

	}

	/**
	 * does return the default root index
	 *
	 * @return
	 * @throws CMSInitializationError
	 */
	public List<HTMLInfo> loadIndex() throws CMSInitializationError, IOException {
		return this.loadIndex("");
	}

	/**
	 * load the index of the given folder
	 */
	public List<HTMLInfo> loadIndex(String parFolder) throws CMSInitializationError, IOException {
		if (rootDirectory == null) {
			throw new CMSInitializationError();
		}

		DirectoryInfo dInfo = loadFiles(parFolder);

		if (dInfo != null) {
			List<HTMLInfo> items = new ArrayList<HTMLInfo>();
			convertDictonary2List(dInfo, items);
			Collections.sort(items, new Comparator<HTMLInfo>() {
				@Override
				public int compare(HTMLInfo htmlInfo1, HTMLInfo htmlInfo2) {
					return htmlInfo1.getFileName().compareTo(htmlInfo2.getFileName());
				}
			});
			return items;
		}
		return null;
	}

	/**
	 * converts the DirectoryInfo to a List<HTMLInfo>
	 *
	 * @param currentDirectory
	 * @param items
	 */
	private void convertDictonary2List(DirectoryInfo currentDirectory, List<HTMLInfo> items) {
		Iterator<FileInfo> iterator = currentDirectory.getFileInfos().values().iterator();
		while (iterator.hasNext()) {
			FileInfo fileInfo = iterator.next();
			if (fileInfo instanceof DirectoryInfo) {
				convertDictonary2List((DirectoryInfo) fileInfo, items);
			} else {
				items.add((HTMLInfo) fileInfo);
			}
		}

	}

	private String readFile(File file) throws IOException {

//BufferedReader reader = new BufferedReader(new FileReader(file));
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf8"));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}

		return stringBuilder.toString();
	}
}
