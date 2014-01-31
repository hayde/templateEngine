/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.hayde.box.template.cms.infos;

import java.util.Date;

/**
 *
 * @author ArmenNalbandyan
 */
public class FileInfo {

	private String fileName;
	private Date created;

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Date getFileCreationDate() {
		return created;
	}

	public void setFileCreationDate(Date created) {
		this.created = created;
	}
}
