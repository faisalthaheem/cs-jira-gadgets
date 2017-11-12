package com.computedsynergy.jira.utils;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;

public class JiraIssueHelper {
	
	//possible use statuses defined in com.atlassian.jira.issue.status.category.StatusCategory
	public static final String STATUS_TODO = "To Do";
	public static final String STATUS_IN_PROGRESS = "In Progress";
	public static final String STATUS_DONE = "Done";
	
	public static final String CUSTOM_FIELD_START_DATE = "Start Date";
	public static final String CUSTOM_FIELD_EFFORT_ALLOCATION_PERCENTAGE = "Effort Allocation Percentage";
	public static final String USERNAME_UNASSIGNED = "Unassigned";
	
	public static final String DAY_OF_WEEK_SAT = "sat";
	public static final String DAY_OF_WEEK_SUN = "sun";
	
	public static CustomField getCustomField(String fieldName){
		
		final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
		final CustomField customField = customFieldManager.getCustomFieldObjectByName(fieldName);
		
		return customField;
	}

}
