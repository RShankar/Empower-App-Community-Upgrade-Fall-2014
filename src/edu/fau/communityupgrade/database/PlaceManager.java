package edu.fau.communityupgrade.database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import edu.fau.communityupgrade.callback.DefaultFindCallback;
import edu.fau.communityupgrade.callback.DefaultSaveCallback;
import edu.fau.communityupgrade.callback.LocationHandlerCallback;
import edu.fau.communityupgrade.helper.ParseHelper;
import edu.fau.communityupgrade.location.LocationHandler;
import edu.fau.communityupgrade.models.Place;

/**
 * This class connects to the Parse Database
 * and handles all DB connections related to Places.
 * 
 * TODO: Add Cache
 * @author kyle
 *
 */
public class PlaceManager {

	//Name of the DB columns
	public final static String OBJECT_ID = "objectId";
	public final static String CREATED_BY = "createdBy";
	public final static String CREATED_AT = "createdAt";
	public final static String NAME = "name";
	public final static String CONTACT_NAME = "contactName";
	public final static String CONTACT_NUMBER = "contactNumber";
	public final static String LOCATION = "location";
		
	public static String TABLE = "Place";
	public static String TAG = "PlaceManager";
	private Context mContext;
	
	
	public PlaceManager(Context context)
	{
		mContext = context;
	}

	/**
	 * Returns an ArrayList of all Places initialized by current
	 * user. Ran in a background thread.
	 * @return
	 */
	public void getAllPlacesCreatedByCurrentUser(DefaultFindCallback<Place> callback)
	{
		UserManager userManager  = UserManager.getInstance();
		ParseUser user = userManager.getParseUser();
		
		if(user == null)
		{
			Log.d(TAG,"No current user logged in. ");
			return;
		}
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE);
		query.include(CREATED_BY);
		query.whereEqualTo(CREATED_BY,user);
		
		query.findInBackground(new PlaceFindCallback(callback));
	}
	
	/**
	 * This method returns all places
	 * within the radius given.
	 * @param radiusInMiles
	 * @param callback
	 */
	public void getAllPlacesNearUser(final double radiusInMiles, 
										final DefaultFindCallback<Place> callback)
	{
		final ParseQuery<ParseObject> mapQuery = ParseQuery.getQuery(TABLE);
		LocationHandler locationHandler = new LocationHandler(mContext);
		locationHandler.updateLocation(new LocationHandlerCallback(){

			@Override
			public void onLocationUpdate(Location location) {
				ParseGeoPoint point = new ParseGeoPoint();
				point.setLatitude(location.getLatitude());
				point.setLongitude(location.getLongitude());
				
				mapQuery.include(CREATED_BY);
				mapQuery.whereWithinMiles(LOCATION, point, radiusInMiles);
				
				mapQuery.findInBackground(new PlaceFindCallback(callback));
			}

			@Override
			public void onProviderNotAvailable() {
				callback.onError("No Provider is available.");
				
			}
		});
		
	}
	
	/**
	 * Adds a place based on the users current location
	 * @param place
	 * @param callback
	 */
	public void SavePlaceFromUserLocation(final Place place, 
											final DefaultSaveCallback<Place> callback )
	{
		final UserManager userManager  = UserManager.getInstance();
		
		LocationHandler locationHandler = new LocationHandler(mContext);
		locationHandler.updateLocation(new LocationHandlerCallback(){

			@Override
			public void onLocationUpdate(Location location) {
				ParseGeoPoint point = new ParseGeoPoint();
				point.setLatitude(location.getLatitude());
				point.setLongitude(location.getLongitude());
				
				ParseObject saveObject = new ParseObject(TABLE);
				
				saveObject.put(LOCATION, point);
				saveObject.put(CREATED_BY, userManager.getParseUser());
				saveObject.put(NAME, place.getName());
				saveObject.put(CONTACT_NAME, place.getContactName());
				saveObject.put(CONTACT_NUMBER, place.getContactNumber());
				saveObject.saveInBackground(new PlaceSaveCallback(callback,saveObject));
			}

			@Override
			public void onProviderNotAvailable() {
				
				
			}
		});
		
	}
	
	
	private class PlaceSaveCallback extends SaveCallback
	{
		final DefaultSaveCallback<Place> callback;
		
		final Place place;
		
		public PlaceSaveCallback(DefaultSaveCallback<Place> c, ParseObject object)
		{
			callback = c;
			place = ParseHelper.parseObjectToPlace(object);
		}

		@Override
		public void done(ParseException e) {
			if(e == null)
			{
				callback.onSaveComplete(place);
			}
			else
			{
				callback.onError(e.toString());
			}
			
		}
	}
	
	/**
	 * This Callback is used to handle the information once the find has 
	 * happened from the previous thread. Requires passing a instance
	 * of DefaultParseCallback to send the information back to the Activity.
	 * @author kyle
	 *
	 */
	private class PlaceFindCallback extends FindCallback<ParseObject>
	{
		DefaultFindCallback<Place> callback;
		
		public PlaceFindCallback(DefaultFindCallback<Place> callback)
		{
			this.callback = callback;
		}
		
		@Override
		public void done(List<ParseObject> list, ParseException e) {
			
			final ArrayList<Place> placesList = new ArrayList<Place>();
			
			if(e != null)
			{
				Log.e(TAG,"Error retrieving places: ",e);
				callback.onError(e.toString());
				return;
			}
			
			CommentManager cManager = new CommentManager();
			
			
			for(int i=0;i<list.size();i++)
			{
				Place place = ParseHelper.parseObjectToPlace(list.get(i));
				place.setComments(cManager.getCommentsByPlace(list.get(i)));
				Log.d(TAG,"TEST: "+place);
				placesList.add(place);
			}
			
			callback.onComplete(placesList);
		}
		
	}
	
	
}
