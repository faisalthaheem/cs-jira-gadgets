package com.computedsynergy.jira.servlet;

import com.computedsynergy.jira.pojos.JiraUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

import com.computedsynergy.jira.ProjectsResource;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.project.ProjectManager;


public class ResourceUtilizationExplorer extends HttpServlet{
    private static final Logger log = LoggerFactory.getLogger(ResourceUtilizationExplorer.class);

	private IssueService issueService;
	private ProjectService projectService;
	private SearchService searchService;
	private UserManager userManager;
	private TemplateRenderer templateRenderer;
	private com.atlassian.jira.user.util.UserManager jiraUserManager;
	private JiraAuthenticationContext authContext;
	private com.atlassian.jira.user.util.UserManager allUsersManager;
	private UserUtil userUtil;
	private VelocityRequestContextFactory velocityRequestContextFactory;
	private ProjectManager projectManager;
	private PermissionManager permissionManager;
	private SearchRequestService searchRequestService;
	private WorklogManager worklogManager;
	
	private static final String INDEX_TEMPLATE = "/templates/index.vm";
	
	public ResourceUtilizationExplorer(
				IssueService issueService, 
				ProjectService projectService, 
                SearchService searchService,
                SearchRequestService searchRequestService,
				UserManager userManager,
                com.atlassian.jira.user.util.UserManager jiraUserManager,
                TemplateRenderer templateRenderer,
				JiraAuthenticationContext authContext,
				com.atlassian.jira.user.util.UserManager allUsersManager,
				UserUtil userUtil,
				VelocityRequestContextFactory velocityRequestContextFactory,
				ProjectManager projectManager,
				PermissionManager permissionManager,
                WorklogManager worklogManager
			) {
				this.issueService = issueService;
				this.projectService = projectService;
				this.searchService = searchService;
				this.userManager = userManager;
				this.templateRenderer = templateRenderer;
				this.jiraUserManager = jiraUserManager;
				this.searchRequestService = searchRequestService;
				
				this.authContext = authContext;
				this.allUsersManager = allUsersManager;
				this.userUtil = userUtil;
				this.velocityRequestContextFactory = velocityRequestContextFactory;
				this.projectManager = projectManager;
				this.permissionManager = permissionManager;
				this.worklogManager = worklogManager;
	}
	
	private User getCurrentUser(HttpServletRequest req) {
		// To get the current user, we first get the username from the session.
		// Then we pass that over to the jiraUserManager in order to get an
		// actual User object.
		return jiraUserManager.getUser(userManager.getRemoteUsername(req));
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
						   throws ServletException, IOException {
							   
		doProcess(req, resp);
	}
	
    @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
						   throws ServletException, IOException {
							   
		doProcess(req, resp);
	}
	
	protected void doProcess(HttpServletRequest req, HttpServletResponse resp) 
						   throws ServletException, IOException {
		
		ProjectsResource projectsResource =  new ProjectsResource(
				searchService, 
				searchRequestService, 
				authContext, 
				userManager, 
				allUsersManager, 
				userUtil, 
				velocityRequestContextFactory, 
				projectManager,
				permissionManager, 
				worklogManager
		);
		
		Map<String, Object> context = Maps.newHashMap();
		
		Set<JiraUser> retUsers = projectsResource.getUsers();
		context.put("users", retUsers);
		
		resp.setContentType("text/html;charset=utf-8");
	    templateRenderer.render(INDEX_TEMPLATE, context, resp.getWriter());
	   
	}

}