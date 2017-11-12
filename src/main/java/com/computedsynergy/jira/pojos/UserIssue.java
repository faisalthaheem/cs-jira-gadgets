package com.computedsynergy.jira.pojos;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.computedsynergy.jira.utils.JiraIssueHelper;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.atlassian.jira.issue.Issue;

import net.jcip.annotations.Immutable;

@Immutable
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class UserIssue {

	private static final Log log = Logger.getInstance(UserIssue.class);
	
	@XmlElement
	private String issueKey;
	
	@XmlElement
	private String self;
	
	@XmlElement
	private String summary;
	
	@XmlElement
	private String startDate;
	
	@XmlElement
	private String dueDate;
	
	private Timestamp stampStartDate;
	private Timestamp stampDueDate;
	
	private static final String ISSUE_BROWSE_URL = "/browse/";
	
	public UserIssue(Issue issue, String baseUrl){
	
		this.issueKey = issue.getKey();
		this.self = baseUrl + ISSUE_BROWSE_URL + this.issueKey;
		this.summary = issue.getSummary();
		
		
		this.stampStartDate = (Timestamp)issue.getCustomFieldValue(JiraIssueHelper.getCustomField(JiraIssueHelper.CUSTOM_FIELD_START_DATE));
		this.stampDueDate = issue.getDueDate();
		
		if(this.stampStartDate == null){
			this.stampStartDate = new Timestamp(new java.util.Date().getTime());
		}
		
		if(this.stampDueDate == null){
			this.stampDueDate = new Timestamp(new java.util.Date().getTime());
		}
		
		this.startDate = new SimpleDateFormat("MMM/dd/yyyy").format(this.stampStartDate);
		this.dueDate = new SimpleDateFormat("MMM/dd/yyyy").format(this.stampDueDate);
	}

	public String getIssueKey() {
		return issueKey;
	}

	public void setIssueKey(String issueKey) {
		this.issueKey = issueKey;
	}

	public String getSelf() {
		return self;
	}

	public void setSelf(String self) {
		this.self = self;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getDueDate() {
		return dueDate;
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	public static String getIssueBrowseUrl() {
		return ISSUE_BROWSE_URL;
	}

	public Timestamp getStampStartDate() {
		return stampStartDate;
	}

	public void setStampStartDate(Timestamp stampStartDate) {
		this.stampStartDate = stampStartDate;
	}

	public Timestamp getStampDueDate() {
		return stampDueDate;
	}

	public void setStampDueDate(Timestamp stampDueDate) {
		this.stampDueDate = stampDueDate;
	}
}
