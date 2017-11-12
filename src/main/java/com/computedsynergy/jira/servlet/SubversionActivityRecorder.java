package com.computedsynergy.jira.servlet;

import java.io.IOException;

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
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;

import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.activeobjects.external.ActiveObjects;
import static com.google.common.base.Preconditions.*;

import com.computedsynergy.jira.ao.SubversionActivityRecord;

//import com.atlassian.sal.api.auth.LoginUriProvider;

public class SubversionActivityRecorder extends ServletBase{
    /**
	 * 
	 */

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ResourceUtilizationExplorer.class);
	
	private  ActiveObjects ao = null;
	
	public SubversionActivityRecorder(
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
				LoginUriProvider loginUriProvider,
				ActiveObjects ao
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
				
			this.ao = checkNotNull(ao);
	
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
		
		
		//todo, filter out the issue key and try to find that in the JIRA list
		final String repoName = req.getParameter("repo");
		final int rev = Integer.parseInt(req.getParameter("rev"));
		final String user = req.getParameter("user");
		final String comments = req.getParameter("comments");
		
		ao.executeInTransaction(new TransactionCallback<SubversionActivityRecord>() // (1)
        {
            @Override
            public SubversionActivityRecord doInTransaction()
            {
                final SubversionActivityRecord record = ao.create(SubversionActivityRecord.class); // (2)
                record.setRepoName(repoName); // (3)
                record.setRevision(rev);
				record.setUser(user);
				record.setComments(comments);
                record.save(); // (4)
                return record;
            }
        });
	}
	
}

