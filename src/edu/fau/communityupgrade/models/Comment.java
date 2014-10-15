package edu.fau.communityupgrade.models;

public class Comment {

	private final String objectId;
	private final String comment_content;
	private final String placeId;
	private final User createdBy;
	private final String parentId;
	
	
	
	public Comment(String objectId, String comment_content, String placeId,
			User createdBy, String parentId) {
		
		this.objectId = objectId;
		this.comment_content = comment_content;
		this.placeId = placeId;
		this.createdBy = createdBy;
		this.parentId = parentId;
	}


	public String getObjectId() {
		return objectId;
	}



	public String getComment_content() {
		return comment_content;
	}



	public String getPlaceId() {
		return placeId;
	}



	public User getCreatedBy() {
		return createdBy;
	}



	public String getParentId() {
		return parentId;
	}
	
	public String toString()
	{
		return comment_content;
		
	}
	
}
