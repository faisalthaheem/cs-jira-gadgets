package com.computedsynergy.jira.servlet;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;


@SuppressWarnings({ "unused", "serial" })
public class ServletBase extends HttpServlet{

	private static final Logger log = LoggerFactory.getLogger(ResourceUtilizationExplorer.class);

	public IssueService issueService;
	public ProjectService projectService;
	public SearchService searchService;
	public UserManager userManager;
	public TemplateRenderer templateRenderer;
	public com.atlassian.jira.user.util.UserManager jiraUserManager;
	public JiraAuthenticationContext authContext;
	public com.atlassian.jira.user.util.UserManager allUsersManager;
	public UserUtil userUtil;
	public VelocityRequestContextFactory velocityRequestContextFactory;
	public ProjectManager projectManager;
	public PermissionManager permissionManager;
	public SearchRequestService searchRequestService;
	public LoginUriProvider loginUriProvider;
	
	
	public ServletBase(
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
				LoginUriProvider loginUriProvider
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
				
				this.loginUriProvider = loginUriProvider; 
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
		
		
		String username = userManager.getRemoteUsername(req);
		if (username == null)
		{
			redirectToLogin(req, resp);
			return;
		}

	}
	
	protected void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
	}
	
	protected URI getUri(HttpServletRequest request)
	{
		StringBuffer builder = request.getRequestURL();
		if (request.getQueryString() != null)
		{
			builder.append("?");
			builder.append(request.getQueryString());
		}
		return URI.create(builder.toString());
	} 
	
}

