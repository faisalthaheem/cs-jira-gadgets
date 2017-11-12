package com.computedsynergy.jira.pojos;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;

@Immutable
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class ProjectRemainingWork {

	@XmlElement
	private String label;
	
	@XmlElement
	private Double remainingWork;
	
	
	public ProjectRemainingWork(){
		
	}
	
	public ProjectRemainingWork(String label, Double remainingWork){
		this.label = label;
		this.remainingWork = remainingWork;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Double getRemainingWork() {
		return remainingWork;
	}

	public void setRemainingWork(Double remainingWork) {
		this.remainingWork = remainingWork;
	}
	
	
}
