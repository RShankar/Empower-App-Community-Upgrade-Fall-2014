package edu.fau.communityupgrade.models;

import java.util.ArrayList;

public class Place {

	private final double latitude, longitude;
	
	private final String name;
	
	private final User createdBy;
	
	private ArrayList<Comment> comments;
	
	public Place(String name, User user, double latitude, double longitude, ArrayList<Comment> comments)
	{
		this.name = name;
		this.createdBy = user;
		this.latitude = latitude;
		this.longitude = longitude;
		this.comments = comments;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getName() {
		return name;
	}

	public User getCreatedBy() {
		return createdBy;
	}
	
	public void setComments(ArrayList<Comment> c)
	{
		this.comments = c;
	}
	
	public String toString()
	{
		return name+", "+latitude+", "+longitude+",USER: "+createdBy.getUsername()+comments.toString();
	}
}
