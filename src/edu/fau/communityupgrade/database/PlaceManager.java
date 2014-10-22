package edu.fau.communityupgrade.database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
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
import edu.fau.communityupgrade.preferences.ApplicationPreferenceManager;

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
	
	
	public static final long CACHE_EXPIRE_TIME_MILLI = 30*1000*60;
	private static final String CACHE_KEY_PLACES_NEAR = "Cache_Key_Places_Near";
	
	private static String LIST_OF_PLACES_NEAR_USER_KEY = "";
	
	private ApplicationPreferenceManager preferenceManager;
	private Context mContext;
	
	
	public PlaceManager(final Context context)
	{
		mContext = context;
		preferenceManager = new ApplicationPreferenceManager(mContext);
	}

	/**
	 * Returns an ArrayList of all Places initialized by current
	 * user. Ran in a background thread.
	 * @return
	 */
	public void getAllPlacesCreatedByCurrentUser(final DefaultFindCallback<Place> callback)
	{
		UserManager userManager  = UserManager.getInstance();
		ParseUser user = userManager.getParseUser();
		
		if(user == null)
		{
			Log.d(TAG,"No current user logged in. ");
			return;
		}
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE);
		query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
		query.include(CREATED_BY);
		query.whereEqualTo(CREATED_BY,user);
		
		query.findInBackground(new PlaceFindByUserCallback(callback));
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
		Log.d(TAG,"getAllPlacesNearUser");
		
		long currentTime = System.currentTimeMillis();
		long lastCacheTime = preferenceManager.getLastPlaceSavedTime();
		final boolean useCached;
		
		
		if(lastCacheTime != -1 && currentTime - lastCacheTime < CACHE_EXPIRE_TIME_MILLI)
		{
			useCached = true;
		}
		else
		{
			useCached = false;
		} 
		
		Log.d(TAG,"lastCacheTime: "+lastCacheTime+", currentTime - lastCacheTime: "+(currentTime - lastCacheTime)+", useCache: "+useCached);

		
		LocationHandler locationHandler = new LocationHandler(mContext);
		locationHandler.updateLocation(new LocationHandlerCallback(){

			@Override
			public void onLocationUpdate(final Location location) {
				getPlacesNearUser(location, radiusInMiles,callback,useCached);
			}

			@Override
			public void onProviderNotAvailable() {
				callback.onProviderNotAvailable();
			}
		});	
	}
	
	/**
	 * Uses location give to retrieve information and pass it to the callback
	 * @param location
	 * @param radiusInMiles
	 * @param callback
	 */
	private void getPlacesNearUser(final Location location, final double radiusInMiles, final DefaultFindCallback<Place> callback, final boolean useCache )
	{		
		final ParseQuery<ParseObject> mapQuery = ParseQuery.getQuery(TABLE);
		
		if(useCache)
		{
			Log.d(TAG,"fromPin");
			mapQuery.fromLocalDatastore();
		}
		
		ParseGeoPoint point = new ParseGeoPoint();
		point.setLatitude(location.getLatitude());
		point.setLongitude(location.getLongitude());
		
		mapQuery.include(CREATED_BY);
		mapQuery.whereWithinMiles(LOCATION, point, radiusInMiles);
		
		mapQuery.findInBackground(new PlaceFindNearCallback(callback,useCache));
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
			public void onLocationUpdate(final Location location) {
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
				callback.onProviderNotAvailable();
			}
		});
	}
	
	/**
	 * This Callback is used when the user wants to save a place.
	 * Once the place has been saved, this callback will be used
	 * to tell the Activity that is was saved or if an error occured.
	 * @author kyle
	 *
	 */
	private class PlaceSaveCallback extends SaveCallback
	{
		final DefaultSaveCallback<Place> callback;
		
		final Place place;
		
		public PlaceSaveCallback(final DefaultSaveCallback<Place> c, final ParseObject object)
		{
			callback = c;
			place = ParseHelper.parseObjectToPlace(object);
		}

		@Override
		public void done(final ParseException e) {
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
	 */
	private class PlaceFindNearCallback extends FindCallback<ParseObject>
	{
		
		final DefaultFindCallback<Place> callback;
		
		public PlaceFindNearCallback(final DefaultFindCallback<Place> callback, boolean c)
		{
			this.callback = callback;
		}
		
		@Override
		public void done(final List<ParseObject> list, final ParseException e) {
			
			Log.d(TAG,"Retrieved Places");
			
			if(e != null)
			{
				Log.e(TAG,"Error retrieving places: ",e);
				callback.onError(e.toString());
				return;
			}
			
			ParseObject.pinAllInBackground(list);
			preferenceManager.setLastPlaceSavedTime();
			
			createListOfPlaces(list,callback);
			
			
		}
		
	}
	
	/**
	 * 
	 * @param places
	 * @param callback
	 */
	private void createListOfPlaces(final List<ParseObject> places,final DefaultFindCallback<Place> callback)
	{
		/**
		 * 
		 */
		new Thread(new Runnable(){

			@Override
			public void run() {
				CommentManager cManager = new CommentManager(mContext);
				final ArrayList<Place> placesList = new ArrayList<Place>();
				
				
				for(int i=0;i<places.size();i++)
				{
					Place place = ParseHelper.parseObjectToPlace(places.get(i));
					place.setComments(cManager.getCommentsByPlace(places.get(i)));
					//Log.d(TAG,"TEST: "+place);
					placesList.add(place);
				}
				
				Log.d(TAG,"callback.onComplete");
			
				Handler mainHandler = new Handler(mContext.getMainLooper());

				//Required to update View
				Runnable myRunnable = new Runnable(){
					@Override
					public void run() {
						callback.onComplete(placesList);
						
					}
				};
				mainHandler.post(myRunnable);	
			}
		}).start();
		
		
	}
	
	
	
	/**
	 * This Callback is used to handle the information once the find has 
	 * happened from the previous thread. Requires passing a instance
	 * of DefaultParseCallback to send the information back to the Activity.
	 * @author kyle
	 *
	 */
	private class PlaceFindByUserCallback extends FindCallback<ParseObject>
	{
		DefaultFindCallback<Place> callback;
		
		public PlaceFindByUserCallback(DefaultFindCallback<Place> callback)
		{
			this.callback = callback;
		}
		
		@Override
		public void done(List<ParseObject> list, ParseException e) {
			if(e != null)
			{
				Log.e(TAG,"Error retrieving places: ",e);
				callback.onError(e.toString());
				return;
			}
			createListOfPlaces(list,callback);
		}
		
	}
	
	
}
