package edu.fau.communityupgrade.models;

import java.util.ArrayList;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Place implements Parcelable {

	private String objectId;
	
	private double latitude, longitude;
	
	private String name;
	
	private User createdBy;
	
	private String description;
	
	private String address;
	
	private String contactNumber;
	
	private ArrayList<Comment> comments;
	
	private Date createdAt;
	
	public Place(final String objectId,final String name, User user, String description, String cNumber, String address, Date cAt, double latitude, double longitude, final ArrayList<Comment> comments)
	{
		this.objectId = objectId;
		this.name = name;
		this.createdBy = user;
		this.latitude = latitude;
		this.longitude = longitude;
		createdAt = cAt;
		if(comments == null)
		{
			this.comments = new ArrayList<Comment>();
		}
		else
		{
			this.comments = comments;
		}
		
		this.description = description;
		this.address = address;
		contactNumber = cNumber;
	}

	protected Place()
	{
		this.objectId = null;
		this.name = null;
		this.createdBy = null;
		this.latitude = 0;
		this.longitude = 0;
			this.comments = new ArrayList<Comment>();
		
		this.description = null;
		this.address = null;
		contactNumber = null;
	}
	
	public String getObjectId() {
		return objectId;
	}
	
	public String getAddress()
	{
		return address;
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getContactName() {
		return description;
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
		
		
		return comments;
	}
	
	/**
	 * Used to update Comments
	 * @param c
	 */
	public void setComments(final ArrayList<Comment> c)
	{
		comments.addAll(c);
	}
	
	protected void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	protected void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	protected void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	protected void setDescription(String description) {
		this.description = description;
	}

	protected void setAddress(String address) {
		this.address = address;
	}

	protected void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	protected void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	
	public String toString()
	{
		String toString = "";
		if(name != null)
			toString += name;
		
		toString += ","+"ObjectID = ";
		if(objectId == null)
			toString += "null";
		else
			toString += objectId;
		
		toString += ", "+latitude+", "+longitude;
		
		if(createdBy != null)
			toString += createdBy.getUsername();
		
		if(comments != null)
			toString += comments.toString();
		
		
		return toString;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(objectId);
		dest.writeString(name);
		dest.writeString(description);
		dest.writeString(contactNumber);
		dest.writeString(description);
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeList(comments);
		dest.writeLong(createdAt.getTime());
		dest.writeParcelable(createdBy, 0);
		
	}
	
	public Place(Parcel in)
	{
		objectId = in.readString();
		name = in.readString();
		description = in.readString();
		contactNumber = in.readString();
		address = in.readString();
		latitude = in.readDouble();
		longitude = in.readDouble();
		
		ArrayList<Comment> d = in.readArrayList(Comment.class.getClassLoader());
		
		comments = new ArrayList<Comment>();
		if(d != null)
		{
			comments.addAll(d);
		}
		
		Date createdAt = new Date(in.readLong());
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
