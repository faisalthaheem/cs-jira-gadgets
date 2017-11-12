package com.computedsynergy.jira.pojos;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


import com.computedsynergy.jira.utils.JiraIssueHelper;
import net.jcip.annotations.Immutable;

import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.atlassian.jira.issue.Issue;

@Immutable
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class ResourceBandwidthStatistic {
	
	private static final Log log = Logger.getInstance(UserStat.class);
	
	@XmlElement
	private int totalAssingedIssues;
	
	@XmlElement
	private int totalAssignedIssuesWithoutDates;
	
	@XmlElement
	private int totalAssignedIssuesWithoutEstimates;

	@XmlElement
	private String displayName;
	
	@XmlElement
	private String emailAddress;
	
	@XmlElement
	private String userName;
	
	//date to amount of work
	@XmlElement
	private Map<String,Double> workTab = new LinkedHashMap<String,Double>();
	
	//date to what work
	@XmlElement
	private Map<String,String> workAssignments = new LinkedHashMap<String,String>();
	
	//date to what work
	@XmlElement
	private String tasksWithoutEstimates = "";
	
	
	//constants
	private static final Double ZERO = 0.0;
	private static final Double WORK_HOURS_PER_DAY = 8.0;
	
	public ResourceBandwidthStatistic(
				String displayName,
				String emailAddress,
				String userName
			){
		this.displayName = displayName;
		this.emailAddress = emailAddress;
		this.userName = userName;
		this.totalAssingedIssues = 0;
		this.totalAssignedIssuesWithoutDates = 0;
		this.totalAssignedIssuesWithoutEstimates = 0;
	}
	
	public void enlistTasks(List<Issue> workItems){
		
		for(Issue workItem : workItems){
			log.debug("Processing work item: " + workItem.getKey());
			totalAssingedIssues ++;
			
			Double effortAllocationPercentage = (Double)workItem.getCustomFieldValue(JiraIssueHelper.getCustomField(JiraIssueHelper.CUSTOM_FIELD_EFFORT_ALLOCATION_PERCENTAGE));
			if(effortAllocationPercentage == null){
				effortAllocationPercentage = 100.0;
			}
			
			Double effortAllocationPerDay = WORK_HOURS_PER_DAY * ( effortAllocationPercentage / 100.0);
			
			Timestamp stampStartDate = (Timestamp)workItem.getCustomFieldValue(JiraIssueHelper.getCustomField(JiraIssueHelper.CUSTOM_FIELD_START_DATE));
			if(null == stampStartDate){
				totalAssignedIssuesWithoutDates++;
				continue;
			}
			Long originalEstimate = workItem.getOriginalEstimate();
			if(null == originalEstimate){
				tasksWithoutEstimates += workItem.getKey() + "\n";
				totalAssignedIssuesWithoutEstimates++;
				continue;
			}
			
			
			Double estimateInHours = millisToHours(originalEstimate);
			Double remainingEstimateInHours = estimateInHours;
			log.debug("remainingEstimateInHours :" + remainingEstimateInHours);
			
			
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(stampStartDate);
			
			gotoNextWorkingDay(cal);
			
			//only proceed with the following code if resource has any work for the date planned
			if(remainingEstimateInHours >= ZERO)
			{
				do{
					log.debug("remainingEstimateInHours :" + remainingEstimateInHours);
					String workForDate = new SimpleDateFormat("MMM dd EEE").format(cal.getTime());
					
					
					//ensure key is present in map
					if(!this.workTab.containsKey(workForDate)){
						this.workTab.put(workForDate, ZERO);
					}
					
					if(!this.workAssignments.containsKey(workForDate)){
						this.workAssignments.put(workForDate, " ");
					}
					
					//which dates have what tasks
					String workAssignmentsForDay = this.workAssignments.get(workForDate);
					workAssignmentsForDay += workItem.getKey() + "\n";
					this.workAssignments.put(workForDate, workAssignmentsForDay);
					
					if(remainingEstimateInHours >= effortAllocationPerDay){
						this.workTab.put(workForDate, roundNumber(this.workTab.get(workForDate) + (Double)effortAllocationPerDay));
					}else{
						this.workTab.put(workForDate, roundNumber(this.workTab.get(workForDate) + remainingEstimateInHours));
					}

					//increment a day
					cal.add(Calendar.DATE, 1);
					gotoNextWorkingDay(cal);
					remainingEstimateInHours-=effortAllocationPerDay;
					
				}while(remainingEstimateInHours > ZERO);
			}
			
		}
	}
	
	/**
	* Rounds the inputValue to two decimal digits
	*/
	public static Double roundNumber(Double inputValue){
		return Math.round(inputValue*100.0)/100.0;
	}
	
	/**
	 * Utility function that advances a day in the calendar until it's weekend.
	 * Todo: add handling for holidays here
	 * @param cal
	 */
	private void gotoNextWorkingDay(Calendar cal){
		
		//make sure this is not a weekend
		boolean isWeekEnd = false;
		SimpleDateFormat sdf = new SimpleDateFormat("EEE");
		do{
			String dayOfWeek = sdf.format(cal.getTime());
			
			if(
					dayOfWeek.equalsIgnoreCase(JiraIssueHelper.DAY_OF_WEEK_SAT) ||
					dayOfWeek.equalsIgnoreCase(JiraIssueHelper.DAY_OF_WEEK_SUN)
			){
				
				isWeekEnd = true;
				cal.add(Calendar.DATE, 1);
			}else{
				isWeekEnd = false;
			}
				
		}while(isWeekEnd == true);
	}
	
	/**
	 * Fills up the date to workTab array to ensure there are no gaps between the consecutive dates.
	 * @param stampStartDate
	 * @param numDays
	 */
	public void setupDates(Date stampStartDate, int numDays){
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(stampStartDate);
		
		for(int i=0; i< numDays; i++){
			
			String workForDate = new SimpleDateFormat("MMM dd EEE").format(cal.getTime());
			
			if(!this.workTab.containsKey(workForDate)){
				this.workTab.put(workForDate, ZERO);
			}
			
			if(!this.workAssignments.containsKey(workForDate)){
				this.workAssignments.put(workForDate, "");
			}
			
			cal.add(Calendar.DATE, 1);
		}
	}
	
	
	/**
	 * Since we may be calculating the workTab for a resource much earlier than
	 * required to be displayed in order to correctly account for the assigned tasks
	 * and planned time, this function discards all workTab items upto the provided
	 * number of days from the beginning
	 * @param numDaysToDiscard
	 */
	public void sanitizeWorkTab(int numDaysFromBeginningToDiscard){
		
		Map<String,Double> sanitizedWorkTab = new LinkedHashMap<String,Double>();
		int i = 0;
		for(Entry<String, Double> dayTab : this.workTab.entrySet()){
			boolean sanitized = true;
			if(i<numDaysFromBeginningToDiscard){
				i++;
				sanitized = false;
			}
			if(sanitized){
				sanitizedWorkTab.put(dayTab.getKey(), dayTab.getValue());
			}
		}
		
		this.workTab = sanitizedWorkTab;
		
		
		//sanitize workAssignments
		Map<String,String> sanitizedWorkAssignments = new LinkedHashMap<String,String>();
		i = 0;
		for(Entry<String, String> dayTab : this.workAssignments.entrySet()){
			boolean sanitized = true;
			if(i<numDaysFromBeginningToDiscard){
				i++;
				sanitized = false;
			}
			if(sanitized){
				sanitizedWorkAssignments.put(dayTab.getKey(), dayTab.getValue());
			}
		}
		
		this.workAssignments = sanitizedWorkAssignments;
		
	}
	
	
	/**
	 * Utility function for convertin milli seconds into hours
	 * @param millis time in milli seconds
	 * @return time in hours
	 */
	public static Double millisToHours(Long millis){
		return ((double)millis/3600.0);
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
