package edu.fau.communityupgrade.models;

/**
 * This class is used to represent the information of a user. This class is not 
 * connected to the database in anyway.
 * @author kyle
 *
 */
public class User {

	private final String username;
	private final String objectId;
	
	public User(final String username, final String objectId)
	{
		this.username = username;
		this.objectId = objectId;
	}
	
	public String getUsername() {
		return username;
	}
	public String getObjectId() {
		return objectId;
	}
}
