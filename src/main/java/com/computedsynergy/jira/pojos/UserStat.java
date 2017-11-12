package com.computedsynergy.jira.pojos;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.computedsynergy.jira.utils.JiraIssueHelper;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.atlassian.jira.issue.Issue;
import net.jcip.annotations.Immutable;

@Immutable
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class UserStat {
	
	private static final Log log = Logger.getInstance(UserStat.class);
	
	@XmlElement
	private String userName;
	
	@XmlElement
	private String userId;
	
	@XmlElement
	private String self;

	@XmlElement
	private int totalIssuesInProject;
	
	@XmlElement
	private int incompleteIssuesCount;
	
	@XmlElement
	private int incompleteIssuesPercentage;
	
	@XmlElement
	private int completedIssuesCount;
	
	@XmlElement
	private int completedIssuesPercentage;
	
	@XmlElement
	private int issuesNotStartedCount;
	
	@XmlElement
	private int issuesNotEndedCount;
	
	@XmlElement
	private int issuesRiskOverdueCount;
	
	@XmlElement
	private List<UserIssue> completedIsssuesList = new ArrayList<UserIssue>(5);
	
	@XmlElement
	private List<UserIssue> issuesNotStartedList = new ArrayList<UserIssue>(5);
	
	@XmlElement
	private List<UserIssue> issuesNotEndedList = new ArrayList<UserIssue>(5);
	
	@XmlElement
	private List<UserIssue> issuesRiskOverdueList = new ArrayList<UserIssue>(5);
	
	private String baseUrl;
	
	
	
	
	
	public UserStat(User assignee, String assigneeId, String baseUrl){
		
		if(assignee != null){
			this.userName = assignee.getName();
		}else{
			this.userName = JiraIssueHelper.USERNAME_UNASSIGNED;
		}
		this.userId = assigneeId;
		this.baseUrl = baseUrl;
		
		this.incompleteIssuesCount = 0;
		this.incompleteIssuesPercentage = 0;
		this.issuesNotStartedCount = 0;
		this.issuesNotEndedCount = 0;
		this.issuesRiskOverdueCount = 0;
	}
	
	public void enlistIssue(Issue issue){
		
		String issueStatus = issue.getStatusObject().getSimpleStatus().getName();

		UserIssue userIssue = new UserIssue(issue, this.baseUrl);		
		
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new java.util.Date());
		//calculate end of today
		cal.add(Calendar.DATE, -1);
		Timestamp stampNow = new Timestamp(cal.getTime().getTime());
		
		//calculate 3 days from today
		cal.add(Calendar.DATE, 4);
		Date threeDaysFromNow = cal.getTime();
		
		log.debug("issue: " + issue.getKey() + " issueStatus: " + issueStatus);
		
		if( (issueStatus.compareTo(JiraIssueHelper.STATUS_TODO) == 0) && userIssue.getStampStartDate().before(stampNow))
		{
			issuesNotStartedList.add(userIssue);
			
		}else if( (issueStatus.compareTo(JiraIssueHelper.STATUS_IN_PROGRESS) == 0) && userIssue.getStampDueDate().before(stampNow))
		{
			issuesNotEndedList.add(userIssue);
			
		}else if(issueStatus.compareTo(JiraIssueHelper.STATUS_IN_PROGRESS) == 0){
			
			//TimeUnit.DAYS.convert(threeDaysFromNow.getTime() - , sourceUnit)
			///is this task due in the next 3 days?
			if(userIssue.getStampDueDate().before(threeDaysFromNow)){
				issuesRiskOverdueList.add(userIssue);
			}
		}else if(issueStatus.compareTo(JiraIssueHelper.STATUS_DONE) == 0)
		{
			completedIsssuesList.add(userIssue);
			
		}
		
		if(issueStatus.compareTo(JiraIssueHelper.STATUS_DONE) == 0){
			
			completedIssuesCount ++;
		}else{
			
			incompleteIssuesCount++;
		}
		
	}
	
	public void finalizeCounts(int totalIssuesInProject){
		
		this.totalIssuesInProject = totalIssuesInProject;
		this.incompleteIssuesPercentage = (int)Math.round((((double)this.incompleteIssuesCount / (double)totalIssuesInProject) * 100.0));
		this.completedIssuesPercentage = (int)Math.round((((double)this.completedIssuesCount / (double)totalIssuesInProject) * 100.0));
		
		log.debug("finalizeCount:totalIssuesInProject: " + totalIssuesInProject);
		log.debug("finalizeCount:incompleteIssuesCount: " + incompleteIssuesCount);
		log.debug("finalizeCount:incompleteIssuesPercentage: " + incompleteIssuesPercentage);
		log.debug("finalizeCount:completedIssuesPercentage: " + completedIssuesPercentage);
		
		
		this.issuesNotStartedCount = this.issuesNotStartedList.size();
		this.issuesNotEndedCount = this.issuesNotEndedList.size();
		this.issuesRiskOverdueCount = this.issuesRiskOverdueList.size();
		this.completedIssuesCount = this.completedIsssuesList.size();
	}
}
