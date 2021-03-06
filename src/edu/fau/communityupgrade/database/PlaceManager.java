package edu.fau.communityupgrade.database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import edu.fau.communityupgrade.callback.DefaultFindCallback;
import edu.fau.communityupgrade.callback.DefaultFindFirstCallback;
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
	public final static String DESCRIPTION = "description";
	public final static String ADDRESS = "address";
	public final static String CONTACT_NUMBER = "contactNumber";
	public final static String LOCATION = "location";
		
	public static String TABLE = "Place";
	public static String TAG = "PlaceManager";
	
	
	public static final long CACHE_EXPIRE_TIME_MILLI = 30*1000*60; //30 minutes
	
	private static final String CACHE_NEARBY_PLACES = "CACHE_NEARBY_PLACES";
	private static final String CACHE_USERS_PLACES = "CACHE_USERS_PLACES";
	
	//private final HashMap<String,ArrayList<Place>> cacheOfPlacesByKey;
	//private final HashMap<String,ParseObject> cacheOfParseObjectsById; 
	
	private CacheManager cacheManager;
	
	private ApplicationPreferenceManager preferenceManager;
	private final CommentManager commentManager;
	private Context mContext;
	
	
	public PlaceManager(final Context context)
	{
		mContext = context;
		preferenceManager = new ApplicationPreferenceManager(mContext);
		preferenceManager.clearLastPlaceSaved();
		commentManager = new CommentManager(mContext);
		cacheManager = CacheManager.getInstance();
	}
	
	protected void getParseObjectPlaceById(final String id, final DefaultFindFirstCallback<ParseObject>  callback)
	{
		if(cacheManager.containsParseObjectId(id))
		{
			callback.onComplete(cacheManager.getParseObjectById(id));
			return;
		}
		
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(TABLE);
		query.fromLocalDatastore();
		query.whereEqualTo(OBJECT_ID, id);
		query.getFirstInBackground(new GetCallback<ParseObject>(){

			@Override
			public void done(ParseObject parseObject, ParseException e) {
				if(e != null)
				{
					callback.onError(e.toString());
					return;
				}
				callback.onComplete(parseObject);
			}
			
			
		});		
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
		query.fromLocalDatastore();
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
		
		if(cacheManager.containsNearbyPlaces())
		{
			Log.d(TAG,"using Cache.");
			ArrayList<ParseObject> places = cacheManager.getNearbyPlaces();
			
			PlaceFindNearUserCallback c = new PlaceFindNearUserCallback(callback);
			c.done(places, null);
			
			return;
		}
		
		LocationHandler locationHandler = new LocationHandler(mContext);
		
		//Update Location
		locationHandler.updateLocation(new LocationHandlerCallback(){

			@Override
			public void onLocationUpdate(final Location location) {
				Log.d(TAG,"getPlacesNearUSer:onLocationUpdate");
				getPlacesNearUser(location, radiusInMiles,callback);
			}

			@Override
			public void onProviderNotAvailable() {
				Log.d(TAG,"getPlacesNearUser:onProviderNotAvailable");
				callback.onProviderNotAvailable();
			}
		});	
	}
	
	/**
	 * Uses location give to retrieve information and pass it to the callback.
	 * @param location
	 * @param radiusInMiles
	 * @param callback
	 */
	private void getPlacesNearUser(final Location location, final double radiusInMiles, final DefaultFindCallback<Place> callback)
	{		
		final ParseQuery<ParseObject> mapQuery = ParseQuery.getQuery(TABLE);
		
		ParseGeoPoint point = new ParseGeoPoint();
		point.setLatitude(location.getLatitude());
		point.setLongitude(location.getLongitude());
		
		mapQuery.include(CREATED_BY);
		mapQuery.whereWithinMiles(LOCATION, point, radiusInMiles);
		mapQuery.addAscendingOrder(CREATED_AT);
		mapQuery.findInBackground(new PlaceFindNearUserCallback(callback));
	}
	
	public void clearCache()
	{
		cacheManager.clearCache();
	}
	
	public void SavePlace(final Place place, final DefaultSaveCallback<Place> callback)
	{
		final UserManager userManager  = UserManager.getInstance();
		
		new AsyncTask<Void,Void,ParseObject>(){

			@Override
			protected ParseObject doInBackground(Void... params) {
				final Address location = getLocationFromAddress(place.getAddress());
				
				Log.d(TAG,"Address2Location: "+location);
				ParseGeoPoint point = new ParseGeoPoint();
				
				point.setLatitude(location.getLatitude());
				point.setLongitude(location.getLongitude());
				
				ParseObject saveObject = new ParseObject(TABLE);
				saveObject.put(LOCATION, point);
				saveObject.put(CREATED_BY, userManager.getParseUser());
				saveObject.put(NAME, place.getName());
				saveObject.put(DESCRIPTION, place.getContactName());
				saveObject.put(ADDRESS, place.getAddress());
				saveObject.put(CONTACT_NUMBER, place.getContactNumber());
				
				return saveObject;
			}
			
			@Override
			protected void onPostExecute(ParseObject saveObject)
			{
				super.onPostExecute(saveObject);
				saveObject.saveInBackground(new PlaceSaveCallback(callback,saveObject));
			}
			
			
		}.execute();
		
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
				saveObject.put(DESCRIPTION, place.getContactName());
				saveObject.put(ADDRESS, place.getAddress());
				saveObject.put(CONTACT_NUMBER, place.getContactNumber());
				saveObject.saveInBackground(new PlaceSaveCallback(callback,saveObject));
			}

			@Override
			public void onProviderNotAvailable() {
				callback.onProviderNotAvailable();
			}
		});
	}
	
	private void updateCache(final List<ParseObject> list)
	{
		if(list == null)
			return;
		
		for(int i=0;i<list.size();i++)
			updateCache(list.get(i));
		
	}
	
	private void updateCache(final ParseObject object)
	{
		if(object == null)
			return;
		
		cacheManager.addPlaceParseObject(object);
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
		final ParseObject object;
		final Place place;
		
		public PlaceSaveCallback(final DefaultSaveCallback<Place> c, final ParseObject object)
		{
			callback = c;
			this.object = object;
			place = ParseHelper.parseObjectToPlace(object);
		}

		@Override
		public void done(final ParseException e) {
			if(e == null)
			{
				updateCache(object);
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
	private class PlaceFindNearUserCallback extends FindCallback<ParseObject>
	{
		
		final DefaultFindCallback<Place> callback;
		
		
		public PlaceFindNearUserCallback(final DefaultFindCallback<Place> callback)
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
			
			cacheManager.addPlaceParseObjectListNearby(list);
			/**
			 ** Thread to convert ParseObjects to Places and retrieve comments from place.
			 */
			new Thread(new Runnable(){

				@Override
				public void run() {
					final ArrayList<Place> placesList = new ArrayList<Place>();
					
					for(int i=0;i<list.size();i++)
					{
						Place place = ParseHelper.parseObjectToPlace(list.get(i));
						place.setComments(commentManager.getCommentsByPlace(list.get(i)));
						placesList.add(place);
					}
					
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
		
	}
	
	/**
	 * Returns Address with Lat/Long from Address String 
	 * @param strAddress
	 * @return
	 */
	public Address  getLocationFromAddress(String strAddress){

		Geocoder coder = new Geocoder(mContext);
		List<Address> address;

		GeoPoint  p1 = null;
		
		try {
		    address = coder.getFromLocationName(strAddress,5);
		    if (address == null) {
		        return null;
		    }
		    
		    
		    Address location = address.get(0);
		    location.getLatitude();
		    location.getLongitude();
		    Log.d(TAG,"ADdress: "+location.toString());
		    return location;
		}
		catch(Exception e)
		{
			Log.e(TAG,"Error converting address to location: ",e);
			return null;
		}
	
	}
	
	/**
	 * This Callback is used to handle the information once the find has 
	 * happened from the previous thread. Requires passing a instance
	 * of DefaultParseCallback to send the information back to the Activity.
	 * @author kyle
	 */
	private class PlaceFindByUserCallback extends FindCallback<ParseObject>
	{
		DefaultFindCallback<Place> callback;
		
		public PlaceFindByUserCallback(DefaultFindCallback<Place> callback)
		{
			this.callback = callback;
		}
		
		@Override
		public void done(final List<ParseObject> list, final ParseException e) {
			ParseObject.pinAllInBackground(list);
			if(e != null)
			{
				Log.e(TAG,"Error retrieving places: ",e);
				callback.onError(e.toString());
				return;
			}
			
			updateCache(list);
			
			/**
			 ** Thread to convert ParseObjects to PLaces and retrieve comments from place.
			 */
			new Thread(new Runnable(){

				@Override
				public void run() {
					final ArrayList<Place> placesList = new ArrayList<Place>();
					
					for(int i=0;i<list.size();i++)
					{
						Place place = ParseHelper.parseObjectToPlace(list.get(i));
						place.setComments(commentManager.getCommentsByPlace(list.get(i)));
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
		
	}
	
	
}
