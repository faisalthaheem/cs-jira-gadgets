package com.computedsynergy.jira;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import com.computedsynergy.jira.pojos.SubversionCommit;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.extras.common.log.Logger.Log;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import net.java.ao.Query;
//import com.atlassian.query.Query;
import com.atlassian.sal.api.user.UserManager;


//subversion activity record
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.activeobjects.external.ActiveObjects;
import static com.google.common.base.Preconditions.*;

import com.computedsynergy.jira.ao.SubversionActivityRecord;


/**
 * REST resource that provides a list of projects in JSON format.
 */
@Path("/misc")
public class MiscResource
{
	
	private static final Log log = Logger.getInstance(MiscResource.class);
	
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
	
	private  ActiveObjects ao = null;
			
    /**
     * Constructor.
     * @param userManager a SAL object used to find remote usernames in
     * Atlassian products
     * @param userUtil a JIRA object to resolve usernames to JIRA's internal
     * {@code com.opensymphony.os.User} objects
     * @param permissionManager the JIRA object which manages permissions
     * for users and projects
     */
    public MiscResource(SearchService searchService,
    						SearchRequestService searchRequestService,
    						JiraAuthenticationContext authContext,
    						UserManager userManager,
    						com.atlassian.jira.user.util.UserManager allUsersManager,
    						UserUtil userUtil,
    						VelocityRequestContextFactory velocityRequestContextFactory,
    						ProjectManager projectManager,
                            PermissionManager permissionManager,
                            WorklogManager worklogManager,
							ActiveObjects ao
							)
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
		
		this.ao = checkNotNull(ao);
    }
	
	@GET
    @Path ("list-subversion-commits")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response listSubversionCommits(@Context HttpServletRequest request) throws SearchException
    {
    	
    	//https://developer.atlassian.com/display/GADGETS/Developing+Gadgets
    	
        //assignee id to stats
        final List<SubversionCommit> svnStats = new ArrayList<SubversionCommit>(100);

		
		//refer to the following for help
		//https://bitbucket.org/activeobjects/ao-dogfood-blog/src/42ae30d7c64e2fcddeb22d0e02fcde11bd55fc79/src/main/java/net/java/ao/blog/service/AoBlogService.java?at=default&fileviewer=file-view-default
		//https://developer.atlassian.com/docs/atlassian-platform-common-components/active-objects/developing-your-plugin-with-active-objects/the-active-objects-library/finding-entities
		//https://developer.atlassian.com/docs/atlassian-platform-common-components/active-objects/developing-your-plugin-with-active-objects/the-active-objects-library
		
		ao.executeInTransaction(new TransactionCallback<Void>() // (1)
        {
            @Override
            public Void doInTransaction()
            {
                for (SubversionActivityRecord rec : ao.find(SubversionActivityRecord.class, Query.select().order("id desc").limit(100)))
                {
					SubversionCommit commit = new SubversionCommit(
						rec.getRepoName(), 
						rec.getRevision(),
						rec.getUser(),
						rec.getComments()
					);
					
					svnStats.add(commit);
                }
                return null;
            }
        });
        
        // return the project representations. JAXB will handle the conversion
        // to XML or JSON.
        return Response.ok(svnStats).build();
    }

    
}
