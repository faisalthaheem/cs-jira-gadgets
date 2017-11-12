package com.computedsynergy.jira.pojos;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.jcip.annotations.Immutable;

@Immutable
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class JiraUser {

	@XmlElement
	private String id;
	
	@XmlElement
	private String label;
	
	@XmlElement
	private String value;
	
	@XmlElement
	private boolean selected;
	
	public JiraUser(String id, String label, String value, boolean selected){
		this.id = id;
		this.label = label;
		this.value = value;
		this.selected = selected;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	
}
