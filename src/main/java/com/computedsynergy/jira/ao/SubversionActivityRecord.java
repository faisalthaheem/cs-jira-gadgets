package com.computedsynergy.jira.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

@Table("SVN_ACT_REC")
@Preload
public interface SubversionActivityRecord extends Entity
{
	//table structure is
	//id
	//repo name
	//revision
	//user
	//comments
	
    String getRepoName();
    void setRepoName(String repoName);
	
    int getRevision();
    void setRevision(int revision);
	
	String getUser();
	void setUser(String user);
	
	String getComments();
	void setComments(String comments);
	
}