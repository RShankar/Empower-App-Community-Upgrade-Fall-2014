package edu.fau.communityupgrade.models;

import java.util.ArrayList;
import java.util.Date;

public class PlaceBuilder {

	private Place place;
	
	public PlaceBuilder()
	{
		place = new Place();
	}
	
	public PlaceBuilder setObjectId(String id)
	{
		place.setObjectId(id);
		return this;
	}
	public PlaceBuilder setName(String n)
	{
		place.setName(n);
		return this;
	}
	
	public PlaceBuilder setAddress(String a)
	{
		place.setAddress(a);
		return this;
	}
	public PlaceBuilder setComments(ArrayList<Comment> comments)
	{
		place.setComments(comments);
		return this;
	}
	public PlaceBuilder setCreatedBy(User u)
	{
		place.setCreatedBy(u);
		return this;
	}
	
	public PlaceBuilder setCreatedAt(Date d)
	{
		place.setCreatedAt(d);
		return this;
	}
	public PlaceBuilder setDescription(String d)
	{
		place.setDescription(d);
		return this;
	}
	
	public PlaceBuilder setLatitude(double d)
	{
		place.setLatitude(d);
		return this;
	}
	public PlaceBuilder setLongitude(double d)
	{
		place.setLongitude(d);
		return this;
	}
	
	public PlaceBuilder setContactNumber(String n)
	{
		place.setContactNumber(n);
		return this;
	}
	
	public Place build()
	{
		return place;
	}
	
	
}
