package com.computedsynergy.jira;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.computedsynergy.jira.pojos.JiraUser;
import com.computedsynergy.jira.pojos.JiraUserComparator;
import com.computedsynergy.jira.pojos.ResourceBandwidthStatisticComparator;
import com.computedsynergy.jira.utils.JiraIssueHelper;
import com.computedsynergy.jira.pojos.ProjectRemainingWork;
import com.computedsynergy.jira.pojos.ResourceBandwidthStatistic;
import com.computedsynergy.jira.pojos.UserStat;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.sal.api.user.UserManager;

/**
 * REST resource that provides a list of projects in JSON format.
 */
@Path("/projects")
public class ProjectsResource
{
	
	private static final Log log = Logger.getInstance(UserStat.class);
	
    private UserManager userManager;
    private com.atlassian.jira.user.util.UserManager allUsersManager;
    private PermissionManager permissionManager;
    private UserUtil userUtil;
	private SearchService searchService;
	private ProjectManager projectManager;
	private VelocityRequestContextFactory velocityRequestContextFactory;
	private JiraAuthenticationContext authContext;
	private SearchRequestService searchRequestService;
	private WorklogManager worklogManager;
	
	private static final String ASSIGNEE_ID_UNASSIGNED = "Unassigned";
	private static final String RESPONSE_KEY_UNFINISHED_TASK_COUNT = "totalUnfinishedTasksCount";
	private static final String REQUEST_PARAMETER_PROJECT_NAME = "projectName";
	private static final String REQUEST_PARAMETER_USER_LIST = "userlist";
	
	private static final Long DEFAULT_ESTIMATE_FOR_TASKS = 144000L; 
			
    /**
     * Constructor.
     * @param userManager a SAL object used to find remote usernames in
     * Atlassian products
     * @param userUtil a JIRA object to resolve usernames to JIRA's internal
     * {@code com.opensymphony.os.User} objects
     * @param permissionManager the JIRA object which manages permissions
     * for users and projects
     */
    public ProjectsResource(SearchService searchService,
    						SearchRequestService searchRequestService,
    						JiraAuthenticationContext authContext,
    						UserManager userManager,
    						com.atlassian.jira.user.util.UserManager allUsersManager,
    						UserUtil userUtil,
    						VelocityRequestContextFactory velocityRequestContextFactory,
    						ProjectManager projectManager,
                            PermissionManager permissionManager,
                            WorklogManager worklogManager)
    {
    	this.authContext = authContext;
		this.searchService = searchService;
        this.userManager = userManager;
        this.allUsersManager = allUsersManager;
        this.userUtil = userUtil;
        this.permissionManager = permissionManager;
        this.projectManager = projectManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.searchRequestService = searchRequestService;
        this.worklogManager = worklogManager;
    }
	
	@GET
    @Path ("projects-activity")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProjectsActivity(@Context HttpServletRequest request) throws SearchException
    {
		//need to send the projects name and activity
		//this is the object returned
    	Map<String, Object> responseMap = new HashMap<String, Object>(3);
		List<String> projectNames = new ArrayList<String>(20);
		List<String> projectActivity = new ArrayList<String>(20);
    	responseMap.put("projectNames", projectNames);
		responseMap.put("projectActivity", projectActivity);
		
		
		List<Project> projects = projectManager.getProjectObjects();
		
		int maxScore = 0;
		
		for(Project p : projects){
			projectNames.add(p.getName());
			
			//find the issues for each project
			List<Issue> projectIssues = getIssueList(
				getRemoteUserNameFromRequest(request), 
				"" + p.getId(), 
				null, 
				true, 
				null, 
				false);
			
			Date today = new Date();
			int totalScore = 0;
			for(Issue issue : projectIssues){
				
				List<Worklog> workLogs = worklogManager.getByIssue(issue);
				//add to the total work logged against project
				for(Worklog worklog : workLogs){
					
					//if this log entry was created in the past 3 days then consider it as a +1
					long diff = today.getTime() - worklog.getCreated().getTime();
					long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
					if( days <= 3){
						++totalScore;
					}
				}
				
			}
			projectActivity.add("" + totalScore);
			
			if(totalScore > maxScore){
				maxScore = totalScore;
			}
		}
		
		if(maxScore < 11){
			maxScore = 11;
		}else{
			maxScore = maxScore + 30;
		}
		
		responseMap.put("maxScore", maxScore);
		
		// return the project representations. JAXB will handle the conversion
        // to XML or JSON.
        return Response.ok(responseMap).build();
	}

    
    @GET
    @Path ("issue-stats")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getIssueStats(@Context HttpServletRequest request) throws SearchException
    {
    	
    	//https://developer.atlassian.com/display/GADGETS/Developing+Gadgets
    	
    	//this is the object returned
    	Map<String, Object> responseMap = new HashMap<String, Object>(3);
    	responseMap.put("projectName", getProjectOrFilterName(request));
    	
        List<Issue> issues = getIssueListByProjectName(request, true);
        String baseUrl = getBaseUrl();
        
        //assignee id to stats
        Map<String, UserStat> userStats = new HashMap<String, UserStat>(10);
        responseMap.put("userStats", userStats);
        
        for(Issue issue : issues){
        	
        	String assigneeId = issue.getAssigneeId();
        	if(assigneeId == null){
        		assigneeId = ASSIGNEE_ID_UNASSIGNED; 
        	}
        	
        	UserStat userStat = null;
        	if(userStats.containsKey(assigneeId)){
        		userStat = userStats.get(assigneeId);
        		
        	}else{
        		userStat = new UserStat(issue.getAssignee(), assigneeId, baseUrl);
        		userStats.put(assigneeId, userStat);
        	}
        	
        	userStat.enlistIssue(issue);
        }
        
        for(UserStat stat : userStats.values()){
        	
        	stat.finalizeCounts(issues.size());
        }
        //put in the total issues
        responseMap.put("totalIssueCount", issues.size());

        // return the project representations. JAXB will handle the conversion
        // to XML or JSON.
        return Response.ok(responseMap).build();
    }
    
    @GET
    @Path ("component-stats")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getComponentStats(@Context HttpServletRequest request) throws SearchException
    {
    	//this is the object returned
    	Map<String, Object> responseMap = new HashMap<String, Object>(3);
    	//contains stats for components
    	Map<String, Integer> componentMap = new HashMap<String, Integer>(10);
    	responseMap.put("stats", componentMap);
    	responseMap.put("projectName", getProjectOrFilterName(request));
    	
    	//the total number of tasks, we only consider todo or in progress tasks
    	Integer totalUnfinishedTaskCount = 0;
    	
    	//get list of issues for this project
    	List<Issue> issues = getIssueListByProjectName(request, false);
    	
    	for(Issue issue : issues){
    		
    		//this issue must be in to-do or in progress state
    		String issueStatus = issue.getStatusObject().getSimpleStatus().getName();
    		if( issueStatus.compareTo(JiraIssueHelper.STATUS_TODO) != 0 && issueStatus.compareTo(JiraIssueHelper.STATUS_IN_PROGRESS) != 0){
    			continue;
    		}
    		totalUnfinishedTaskCount++;
    		
    		List<ProjectComponent> issueComponents = new ArrayList<ProjectComponent>(issue.getComponentObjects());
    		
    		for(ProjectComponent projectComponent : issueComponents){
    			
    			//Ensure the key exists for later usage
    			if(!componentMap.containsKey(projectComponent.getName())){
    				componentMap.put(projectComponent.getName(), 0);
    			}
    			
    			//Increment count
    			Integer cntComponentTasks = componentMap.get(projectComponent.getName());
    			cntComponentTasks++;
    			componentMap.put(projectComponent.getName(), cntComponentTasks);
    		}
    	}
    	
    	//put in the total tasks
    	responseMap.put(RESPONSE_KEY_UNFINISHED_TASK_COUNT, totalUnfinishedTaskCount);
    	
    	
    	return Response.ok(responseMap).build();
    }
    
    @SuppressWarnings("deprecation")
	@GET
    @Path ("resource-utilization")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getResourceUtilization(@Context HttpServletRequest request) throws SearchException
    {
    	
    	Map<String, Object> responseMap = new HashMap<String, Object>(50);
    	List<ResourceBandwidthStatistic> bandwidthStatistics = new ArrayList<ResourceBandwidthStatistic>(50);
    	
    	responseMap.put("bandwidthStatistics", bandwidthStatistics);
    	
    	//get the list of users stats have been requested for
    	String requestedUsers =  getFirstParamValueFromReqeust(request, REQUEST_PARAMETER_USER_LIST);
    	log.debug("requested utilization for resources: " + requestedUsers);
    	List<String> requestedUsersList = Arrays.asList(requestedUsers.split("\\|"));
    	
    	
    	
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -90);
        Date issueQueryStartFromDate = cal.getTime();
    	
    	if (permissionManager.hasPermission(Permissions.USER_PICKER, authContext.getUser()))
    	{
	    	List<User> appUsers = new ArrayList<User>(allUsersManager.getUsers());
	    	responseMap.put("countUsers", allUsersManager.getTotalUserCount());
	    	
	    	for(User user: appUsers){
	    		
	    		if(!requestedUsersList.contains(user.getEmailAddress())){
	    			//skip as this user's utilization is not required
	    			log.debug("Skipping user: " + user.getEmailAddress());
	    			continue;
	    		}
	    		
	    		ResourceBandwidthStatistic stat = new ResourceBandwidthStatistic(user.getDisplayName(), user.getEmailAddress(), user.getName());
	    		bandwidthStatistics.add(stat);
	    		
	    		List<Issue> userTasks = getIssueList(getRemoteUserNameFromRequest(request), null, stat.getUserName(), true, issueQueryStartFromDate, false);
	    		log.debug("Got " + userTasks.size() + " tasks for user: " + stat.getUserName());
	    		
	    		stat.setupDates(issueQueryStartFromDate, 120);
	    		stat.enlistTasks(userTasks);
	    		stat.sanitizeWorkTab(85);
	    	}
	    	
	    	Collections.sort(bandwidthStatistics, new ResourceBandwidthStatisticComparator());
    	}else{
    		responseMap.put("error", "You need browse user permission.");
    	}
    	
    	return Response.ok(responseMap).build();
    }
    
    
    @SuppressWarnings("deprecation")
	@GET
    @Path ("user-list")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getUserList(@Context HttpServletRequest request){
    
    	Set<JiraUser> retUsers = getUsers();
    	
    	return Response.ok(retUsers).build();
	    	
    }
    
    @SuppressWarnings("deprecation")
	@GET
    @Path ("get-project-remaining-work")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProjectRemainingWork(@Context HttpServletRequest request) throws SearchException{

    	//this is the object returned
    	Map<String, Object> responseMap = new HashMap<String, Object>(3);
    	
    	//version - release date to remaining man hours
    	List<ProjectRemainingWork> remainingWork = new ArrayList<ProjectRemainingWork>();
    	
    	String projectName = getProjectOrFilterName(request);

    	responseMap.put("stats", remainingWork);
    	responseMap.put("projectName", projectName);
    	
    	Query query = JqlQueryBuilder.newBuilder().where().project(Long.valueOf(getProjectOrFilterIdFromReqeust(request))).defaultAnd().buildQuery();
		
		User user = userUtil.getUser(getRemoteUserNameFromRequest(request));
		SearchResults searchResults = searchService.search(user, query, PagerFilter.getUnlimitedFilter());
        List<Issue> issues = searchResults.getIssues();
        //log.debug("Found " + issues.size() + " issues for project.");

        Project proj = projectManager.getProjectObj(Long.valueOf(getProjectOrFilterIdFromReqeust(request)));
        ArrayList<Version> projVersions = new ArrayList<Version>(proj.getVersions());
		
        Date dtToday = new Date();
        SimpleDateFormat versionDateFormatter = new SimpleDateFormat("MMM dd EEE");
		
		
		for(Version projVersion : projVersions){
			Date dtRelease = projVersion.getReleaseDate();
			
			Double projVersionTotalWork = 0.0;
			Double projVersionLoggedWork = 0.0;
			
			if(dtRelease == null){
				continue;
			}
			
			if(dtRelease.getYear() == dtToday.getYear()){
				//log.debug("Processing version: " + projVersion.getName());
				
				//get all issues in this version
		        for(Issue issue : issues){
		        	List<Version> issueVersions = new ArrayList<Version>(issue.getFixVersions());
		        	//log.debug("Issue " + issue.getKey() + " belongs to [" +  issueVersions.size() + "] versions.");
		        	
		        	for(Version issueVersion : issueVersions){
		        		if(issueVersion.getName().equalsIgnoreCase(projVersion.getName())){
		        			
		        			//log.debug("Processing issue: " + issue.getKey());
		        			
		        			projVersionTotalWork += ResourceBandwidthStatistic.millisToHours(
		        					(issue.getOriginalEstimate() == null) ? DEFAULT_ESTIMATE_FOR_TASKS : issue.getOriginalEstimate()
		        			);
		        			//log.debug(issue.getKey() + " " + ResourceBandwidthStatistic.millisToHours(issue.getOriginalEstimate()));

		        			//get all work logged against this issue
		        			List<Worklog> workLogs = worklogManager.getByIssue(issue);
		        			
		        			//add to the total work logged against project
		        			for(Worklog worklog : workLogs){
		        				
		        				projVersionLoggedWork += ResourceBandwidthStatistic.millisToHours(worklog.getTimeSpent());
		        			}
		        		}
		        	}
		        }
			}
			
			String versionToBeReleased = projVersion.getName() + "\n" + versionDateFormatter.format(projVersion.getReleaseDate());
			
			remainingWork.add(
					new ProjectRemainingWork(
							versionToBeReleased,
							ResourceBandwidthStatistic.roundNumber(projVersionTotalWork - projVersionLoggedWork)
					)
			);
			
		}
		
		return Response.ok(responseMap).build();
	    	
    }
	
	public Set<JiraUser> getUsers(){
		
		Set<JiraUser> retUsers = new TreeSet<JiraUser>(new JiraUserComparator());
    	
    	if (permissionManager.hasPermission(Permissions.USER_PICKER, authContext.getUser()))
    	{
    		List<User> appUsers = new ArrayList<User>(allUsersManager.getUsers());

    		for(User u: appUsers){
	    		JiraUser user = new JiraUser(
	    							u.getEmailAddress(),
	    							u.getDisplayName(),
	    							u.getEmailAddress(),
	    							false
	    						);
	    		
	    		retUsers.add(user);
	    	}
    	}
		
		return retUsers;
	}
    
    
    /**
     * Helper function that returns the base url for the application.
     * Useful for creating html links for the issues etc.
     * @return
     */
    private String getBaseUrl()
    {
        return velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
    }
    
    /**
     * Returns the issues by searching on the provided parameters
     * @param request the request to fetch parameters from
     * @param includeResolved whether to include issues which have been resolved
     * @return
     * @throws SearchException
     */
    private List<Issue> getIssueListByProjectName(@Context HttpServletRequest request, boolean includeResolved) throws SearchException{
//rename this method to include filter

    	String projectOrFilterID = getProjectOrFilterIdFromReqeust(request);
    	boolean filterRequested = false;
    	
    	if(isProjectRequested(request)){
    		filterRequested = false;
    	}else{
    		filterRequested = true;
    	}
        
    	return getIssueList(getRemoteUserNameFromRequest(request), projectOrFilterID, null, includeResolved, null, filterRequested);
    }
    
    
    private List<Issue> getIssueList(String remoteUserName, 
    		String projectOrFilterId, 
    		String userId, 
    		boolean includeResolved,
    		Date startDateEqualOrAfter,
    		boolean filterRequested
    		) throws SearchException
    {
    	
    	
    	log.debug("remoteUserName :" + remoteUserName);
    	log.debug("projectOrFilterId :" + projectOrFilterId);
    	log.debug("userId :" + userId);
    	log.debug("includeResolved :" + includeResolved);
    	if(null!=startDateEqualOrAfter){
    		log.debug("startDateEqualOrAfter :" + new SimpleDateFormat("MMM/dd/YYYY").format(startDateEqualOrAfter));
    	}
        // get the corresponding com.opensymphony.os.User object for
        // the request
        User user = userUtil.getUser(remoteUserName);
                
    	Query query = null;
    	
    	if(!filterRequested){
	    	if(projectOrFilterId != null){
		        
	    		if(includeResolved){
		        	query = JqlQueryBuilder.newBuilder().where().project(projectOrFilterId).defaultAnd().buildQuery();
		        }else{
		        	query = JqlQueryBuilder.newBuilder().where().project(projectOrFilterId).defaultAnd().unresolved().buildQuery();
		        }
		        
	    	}else if(userId != null){
	    		
	    		//need to improve the query building logic
	    		if(startDateEqualOrAfter != null){
	    			
	    			CustomField csStartDate = JiraIssueHelper.getCustomField(JiraIssueHelper.CUSTOM_FIELD_START_DATE);
	    			query = JqlQueryBuilder.newClauseBuilder()
	    						.assigneeUser(userId).and()
	    						.customField(csStartDate.getIdAsLong()).gt(startDateEqualOrAfter.getTime())
	    						.buildQuery();
	    			
	    			
	    		}else{
	    		
		    		if(includeResolved){
			        	query = JqlQueryBuilder.newBuilder().where().assigneeUser(userId).defaultAnd().buildQuery();
			        }else{
			        	query = JqlQueryBuilder.newBuilder().where().assigneeUser(userId).defaultAnd().unresolved().buildQuery();
			        }
	    		}
	    		
	    	}
    	}else{
    		query = JqlQueryBuilder.newClauseBuilder().savedFilter(projectOrFilterId).buildQuery(); 
    	}
    	
        SearchResults searchResults = searchService.search(user, query, PagerFilter.getUnlimitedFilter());
        
        
        List<Issue> issues = searchResults.getIssues();
        
        return issues;
    }
    
    
    /**
     * Returns the name of the requesting user by examining the request object
     * @param request
     * @return
     */
    private String getRemoteUserNameFromRequest(@Context HttpServletRequest request){
    	//the request was automatically injected with @Context, so
        // we can use SAL to extract the username from it
        String thisUserName = userManager.getRemoteUsername(request);
        
        return thisUserName;
    }
    
    /**
     * Extracts the project or filter id from request parameter. These are posted by the gadget as
     * project-123123 or filter-123123
     * 
     * @param request
     * @return true if requested was a project
     */
    private boolean isProjectRequested(@Context HttpServletRequest request){
    	String param = getFirstParamValueFromReqeust(request, REQUEST_PARAMETER_PROJECT_NAME);

    	if(param.startsWith("project-")){
    		return true;
    		
        }    	
    	return false;
    }
    
    /**
     * Extracts the project or filter id from request parameter. These are posted by the gadget as
     * project-123123 or filter-123123
     * 
     * @param request
     * @return true if requested was a project
     */
    private String getProjectOrFilterIdFromReqeust(@Context HttpServletRequest request){
    	String param = getFirstParamValueFromReqeust(request, REQUEST_PARAMETER_PROJECT_NAME);

    	if(param.startsWith("project-")){
    		param = param.replace("project-", "");
    		
        }else if(param.startsWith("filter-")){
        	param = param.replace("filter-", "");
    		
        }	
    	return param;
    }
    
    private String getFirstParamValueFromReqeust(@Context HttpServletRequest request, String paramKey){
    	String ret = "";
    	
    	if(request.getParameterMap().containsKey(paramKey)){
    		ret = request.getParameterValues(paramKey)[0];
    	}
    	
    	return ret;
    }
    
    
    
    @SuppressWarnings("deprecation")
	private String getProjectOrFilterName(@Context HttpServletRequest request){
    	String ret = "Unknow Project";
    	
    	String projectOrFilterId = "";
    	
    	String paramProjectOrFilter = getFirstParamValueFromReqeust(request, REQUEST_PARAMETER_PROJECT_NAME);

    	if(paramProjectOrFilter.startsWith("project-")){
    		
    		projectOrFilterId = paramProjectOrFilter.replace("project-", "");
    		
    		final Project project = projectManager.getProjectObj(Long.valueOf(projectOrFilterId));
            if (project != null)
            {
                return project.getName();
            }
    		
        }else if(paramProjectOrFilter.startsWith("filter-")){
        	projectOrFilterId = paramProjectOrFilter.replace("filter-", "");
        	
        	final Long filterId = new Long(projectOrFilterId);

            final SearchRequest sr = searchRequestService.getFilter(
                    new JiraServiceContextImpl(authContext.getLoggedInUser(), new SimpleErrorCollection()), filterId);
            if (sr != null)
            {
                return sr.getName();
            }
            else
            {
                throw new IllegalArgumentException("Unknown filter " + filterId);
            }
        }
    	
    	
    	
    	return ret;
    }
}
