package com.computedsynergy.jira.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;


//import com.atlassian.sal.api.auth.LoginUriProvider;

public class ReportQuarterlyVersion extends ServletBase{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3516313546809313046L;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ResourceUtilizationExplorer.class);
	private static final String TEMPLATE_REPORT_QUARTERLY_VERSION = "/templates/reports/report-quarterly-version.vm";
	
	public ReportQuarterlyVersion(
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
		
				super(
					issueService,
					projectService,
					searchService,
					searchRequestService,
					userManager,
					jiraUserManager,
					templateRenderer,
					authContext,
					allUsersManager,
					userUtil,
					velocityRequestContextFactory,
					projectManager,
					permissionManager,
					loginUriProvider
				);
	
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
		
		super.doProcess(req, resp);
		
		List<String> q1Projects = new ArrayList<String>();
		List<String> q2Projects = new ArrayList<String>();
		List<String> q3Projects = new ArrayList<String>();
		List<String> q4Projects = new ArrayList<String>();

		List<Project> projectsList = projectManager.getProjectObjects();
		
		Date dtToday = new Date();
		
		for(Project proj : projectsList){
			List<Version> projVersions = new ArrayList(proj.getVersions());
			
			for(Version version : projVersions){
				Date dtRelease = version.getReleaseDate();
				
				if(dtRelease == null){
					continue;
				}
				
				if(dtRelease.getYear() == dtToday.getYear()){
					
					switch(dtRelease.getMonth()){
					
						case 0:
						case 1:
						case 2:
							q1Projects.add(proj.getName() + " - " + version.getName());
							break;
							
						case 3:
						case 4:
						case 5:
							q2Projects.add(proj.getName() + " - " + version.getName());
							break;
							
						case 6:
						case 7:
						case 8:
							q3Projects.add(proj.getName() + " - " + version.getName());
							break;
							
						case 9:
						case 10:
						case 11:
							q4Projects.add(proj.getName() + " - " + version.getName());
							break;
					}
				}
			}
		}
		
		
		
		// Create an empty context map to pass into the render method
		Map<String, Object> context = Maps.newHashMap();
		context.put("q1Projects", q1Projects);
		context.put("q2Projects", q2Projects);
		context.put("q3Projects", q3Projects);
		context.put("q4Projects", q4Projects);
		
		// Make sure to set the contentType otherwise bad things happen
		resp.setContentType("text/html;charset=utf-8");
		// Render the velocity template (new.vm). Since the new.vm template 
		// doesn't need to render any in dynamic content, we just pass it an empty context
	    templateRenderer.render(TEMPLATE_REPORT_QUARTERLY_VERSION, context, resp.getWriter());
	}
	
}

