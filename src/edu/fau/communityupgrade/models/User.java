package edu.fau.communityupgrade.models;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * This class is used to represent the information of a user. This class is not 
 * connected to the database in anyway.
 * Implements Parcelable to allow passing of User object around.
 * @author kyle
 */
public class User implements Parcelable {

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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(username);
		dest.writeString(objectId);
	}
	
	public User(Parcel in)
	{
		this.username = in.readString();
		this.objectId = in.readString();
		
	}
	
	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
