package com.computedsynergy.jira.pojos;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;

@Immutable
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class SubversionCommit {
	
	
	@XmlElement
	private String repoName;
	
	@XmlElement
	private int revision;
	
	@XmlElement
	private String user;

	@XmlElement
	private String comments;
	
	
	public SubversionCommit(String repoName, int rev, String user, String comments){
		
		this.repoName = repoName;
		this.revision = rev;
		this.user = user;
		this.comments = comments;
	}
	
}
