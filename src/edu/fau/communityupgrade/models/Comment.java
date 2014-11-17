package edu.fau.communityupgrade.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Comment implements Parcelable, Comparable<Comment> {

	//Used for status of currentUser on comment
	public enum VoteStatus {NONE,UPVOTE,DOWNVOTE};
	
	private final String objectId;
	private final String comment_content;
	private final String placeId;
	private final User createdBy;
	private final String parentId;
	private final double score;
	
	private final Vote userVote;
	
	
	public Comment(String objectId, String comment_content, String placeId,
			User createdBy, String parentId,double score, final Vote uVote) {
		
		this.objectId = objectId;
		this.comment_content = comment_content;
		this.placeId = placeId;
		this.createdBy = createdBy;
		this.parentId = parentId;
		
		if(uVote == null){
			this.userVote = new Vote(createdBy.getObjectId(),this.objectId);
		}
		else
		{
			this.userVote = uVote;
		}
		this.score = score;
	}
  

	public double getScore() {
		return score;
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
}
