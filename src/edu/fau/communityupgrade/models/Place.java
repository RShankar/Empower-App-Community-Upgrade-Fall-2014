package edu.fau.communityupgrade.models;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class Place implements Parcelable {

	private final String objectId;
	
	private final double latitude, longitude;
	
	private final String name;
	
	private final User createdBy;
	
	private final String contactName;
	
	private final String contactNumber;
	
	private ArrayList<Comment> comments;
	
	public Place(String objectId,String name, User user, String cName, String cNumber, double latitude, double longitude, ArrayList<Comment> comments)
	{
		this.objectId = objectId;
		this.name = name;
		this.createdBy = user;
		this.latitude = latitude;
		this.longitude = longitude;
		this.comments = comments;
		contactName = cName;
		contactNumber = cNumber;
	}

	public String getObjectId() {
		return objectId;
	}


	public String getContactName() {
		return contactName;
	}

	public String getContactNumber() {
		return contactNumber;
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
	

	public ArrayList<Comment> getComments() {
		
		if(comments == null)
		{
			comments = new ArrayList<Comment>();
		}
		
		return comments;
	}
	
	/**
	 * Used to update Comments
	 * @param c
	 */
	public void setComments(ArrayList<Comment> c)
	{
		comments = c;
	}
	
	public String toString()
	{
		return name+", "+latitude+", "+longitude+",USER: "+createdBy.getUsername()+comments.toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(objectId);
		dest.writeString(name);
		dest.writeString(contactName);
		dest.writeString(contactNumber);
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeParcelable(createdBy, 0);
		
	}
	
	public Place(Parcel in)
	{
		objectId = in.readString();
		name = in.readString();
		contactName = in.readString();
		contactNumber = in.readString();
		latitude = in.readDouble();
		longitude = in.readDouble();
		createdBy = in.readParcelable(User.class.getClassLoader());
	}
	
	/**
	 * Required Create Class for creating Places from Parcelable
	 */
	public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
}
