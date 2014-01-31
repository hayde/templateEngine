/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.hayde.box.template.cms.infos;

import java.util.HashMap;

/**
 *
 * @author ArmenNalbandyan
 */
public class DirectoryInfo extends FileInfo {

	private HashMap<String, FileInfo> fileInfos = new HashMap<String, FileInfo>();

	public HashMap<String, FileInfo> getFileInfos() {
		return this.fileInfos;
	}

	public void addFileInfo(FileInfo fileInfo) {
		this.fileInfos.put(fileInfo.getFileName(), fileInfo);
	}

	public FileInfo getFileInfo(String name) {
		return this.fileInfos.get(name);
	}
}
