package edu.fau.communityupgrade.models;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Comment implements Parcelable, Comparable<Comment> {

	//Used for status of currentUser on comment
	public enum VoteStatus {NONE,UPVOTE,DOWNVOTE};
	
	private String objectId;
	private String comment_content;
	private String placeId;
	private User createdBy;
	private String parentId;
	private double score;
	private Date createdAt;
	
	private Vote userVote;
	
	
	public Comment(String objectId, String comment_content, String placeId,
			User createdBy, String parentId,double score, Date created, final Vote uVote) {
		
		this.objectId = objectId;
		this.comment_content = comment_content;
		this.placeId = placeId;
		this.createdBy = createdBy;
		this.parentId = parentId;
		this.createdAt = created;
		if(uVote == null){
			this.userVote = new Vote(createdBy.getObjectId(),this.objectId);
		}
		else
		{
			this.userVote = uVote;
		}
		this.score = score;
	}
	
	protected Comment()
	{
		this.objectId = null;
		this.comment_content = null;
		this.placeId = null;
		this.createdBy = null;
		this.parentId = null;
		this.createdAt = null;
			this.userVote = null;
		this.score = 0;
	}
  
	public Date getCreatedAt()
	{
		return this.createdAt;
	}
	
	public double getScore() {
		return score;
	}
	
	public void setScore(final double s)
	{
		score = s;
	}


	public String getObjectId() {
		return objectId;
	}



	public String getComment_content() {
		return comment_content;
	}

	public Vote getUserVote()
	{
		return userVote;
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
		return "ObjectId:"+objectId+", "+comment_content+", score:"+score+" by "+createdBy.getUsername()+", parentID: "+parentId;
	}

	@Override
	public int describeContents() {
		return 0;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(comment_content);
		dest.writeString(objectId);
		dest.writeString(parentId);
		dest.writeString(placeId);
		dest.writeDouble(score);
		dest.writeLong(createdAt.getTime());
		dest.writeParcelable(createdBy, 0);
		dest.writeParcelable(userVote, 0);
	}
	
	//Parcel input Constructor
	public Comment(Parcel in)
	{
		comment_content = in.readString();
		objectId = in.readString();
		parentId = in.readString();
		placeId = in.readString();
		score = in.readDouble();
		createdAt = new Date(in.readLong());
		createdBy = in.readParcelable(User.class.getClassLoader());
		userVote = in.readParcelable(Vote.class.getClassLoader());
	}
	
	/**
	 * Required Create Class for creating Comments from Parcelable
	 */
	public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    @Override
    public boolean equals(Object another)
    {
    	if(another.getClass().isInstance(this.getClass()))
    	{
    		return this.getObjectId().equals(((Comment)another).getObjectId());
    	}
    	else
    	{
    		return this == another;
    	}
    }

	@Override
	public int compareTo(Comment another) {
		return this.getObjectId().compareTo(another.getObjectId());
	}

	protected void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	protected void setComment_content(String comment_content) {
		this.comment_content = comment_content;
	}

	protected void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	protected void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	protected void setParentId(String parentId) {
		this.parentId = parentId;
	}

	protected void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	protected void setUserVote(Vote userVote) {
		this.userVote = userVote;
	}
	
	
	
}
