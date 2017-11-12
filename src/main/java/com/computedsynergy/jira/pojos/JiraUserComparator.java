package com.computedsynergy.jira.pojos;

import java.util.Comparator;

public class JiraUserComparator implements Comparator<JiraUser> {

	@Override
	public int compare(JiraUser arg0, JiraUser arg1) {
		
		return arg0.getId().compareToIgnoreCase(arg1.getId());
	}

}
