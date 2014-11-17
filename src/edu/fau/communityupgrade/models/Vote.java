package edu.fau.communityupgrade.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Vote implements Parcelable {
	
	
	private boolean isUpvote;
	private boolean isSet;
	private final String objectId;
	private final String userId;
	private final String commentId;
	
	public Vote(String userId, String commentId)
	{
		this.isSet = false;
		this.userId = userId;
		this.commentId = commentId;
		this.objectId = null;
	}
	public Vote(String objectId, String userId, String commentId, boolean isUpvote)
	{
		this.isUpvote = isUpvote;
		this.objectId = objectId;
		this.userId = userId;
		this.commentId = commentId;
		this.isSet = true;
	}

	public boolean isUpvote() {
		return isUpvote;
	}
	
	public boolean isSet()
	{
		return isSet;
	}
	
	public void removeVote()
	{
		this.isSet = false;
	}
	
	public void setVoteType(boolean upvote)
	{
		this.isSet = true;
		this.isUpvote = upvote;
	}

	public String getObjectId() {
		return objectId;
	}

	public String getUserId() {
		return userId;
	}
	
	public String getCommentId()
	{
		return commentId;
	}
	@Override
	public int describeContents() {
		
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte((byte) (this.isUpvote ? 1 : 0));    
		dest.writeByte((byte) (this.isSet ? 1 : 0));
		dest.writeString(objectId);
		dest.writeString(this.userId);
		dest.writeString(this.commentId);
	}
	
	public Vote(final Parcel in)
	{
		this.isUpvote = in.readByte() != 0;
		this.isSet = in.readByte() != 0;
		this.objectId = in.readString();
		this.userId = in.readString();
		this.commentId = in.readString();
	}
	
	/**
	 * Required Create Class for creating Comments from Parcelable
	 */
	public static final Parcelable.Creator<Vote> CREATOR = new Parcelable.Creator<Vote>() {
        public Vote createFromParcel(Parcel in) {
            return new Vote(in);
        }
        public Vote[] newArray(int size) {
            return new Vote[size];
        }
    };
}
