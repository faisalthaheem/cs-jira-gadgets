package com.computedsynergy.jira.pojos;

import java.util.Comparator;

public class ResourceBandwidthStatisticComparator implements Comparator<ResourceBandwidthStatistic> {

	@Override
	public int compare(ResourceBandwidthStatistic arg0,
			ResourceBandwidthStatistic arg1) {
		
		return arg0.getDisplayName().compareToIgnoreCase(arg1.getDisplayName());
	}

}
