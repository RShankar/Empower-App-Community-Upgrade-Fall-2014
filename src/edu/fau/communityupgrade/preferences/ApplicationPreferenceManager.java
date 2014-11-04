package edu.fau.communityupgrade.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import edu.fau.communityupgrade.database.CommentManager;
import edu.fau.communityupgrade.location.LocationHandler;

/**
 * This is a class to handle all of the preferences in the 
 * SharedPreferences for the application.
 * @author kyle
 *
 */
public class ApplicationPreferenceManager {

	private static final String USER_PREFERENCE_FILE = "UserPreferences"; 
	private static final String LOCATION_PREFERENCE_FILE = "LocationPreferences";
	private static final String PLACE_PREFERENCE_FILE = "PlacePreferences";
	private static final String COMMENT_PREFERENCE_FILE = "CommentPreferences";
	private static final String USER_PREFERENCE_SESSION_ID = "UserSessionID";
	private static final String USER_PREFERENCE_SESSION_SET_TIME = "UserSessionSetTime";
	
	public static final long USER_PREFERENCE_EMPTY_SESSION_TIME = -1;
	public static final long LOCATION_NOT_SET = -1;

	private final static String CACHE_KEY_LOCATION_TIME_SAVED = "Cache_Key_Places_Time";
	private final static String CACHE_KEY_LOCATION_LATITUDE = "Cache_Key_Places_Near_Lat";
	private final static String CACHE_KEY_LOCATION_LONGITUDE = "Cache_Key_Places_Near_Long";
	
	private final static String PLACE_KEY_TIME_SAVED = "Place_Key_Saved";
	private final static String CACHE_KEY_COMMENTS = "Cache_Key_Comments";
	
	private static final String TAG = "ApplicationPreferenceManager";
	
	private final Context context;
	private SharedPreferences mUserPreferences;
	
	public ApplicationPreferenceManager(Context context)
	{
		this.context = context;
		mUserPreferences = context.getSharedPreferences(
				USER_PREFERENCE_FILE, Context.MODE_PRIVATE);
	}
	
	/**
	 * Returns the sessionID stored in the user preferences.
	 *  If it does not exist, null is returned
	 * @return
	 */
	public String getUserSessionId()
	{
		
		String sessionID = mUserPreferences.getString(USER_PREFERENCE_SESSION_ID, null);
		return sessionID;
	}
	
	/**
	 * Retrieve the last time the user was authenticated.
	 * Used to check if user authetication should be done again.
	 * @return
	 */
	public long getUserSessionSetTime()
	{
		long time = mUserPreferences.getLong(USER_PREFERENCE_SESSION_SET_TIME, -1);
		return time;
	}
	
	/**
	 * Set the last time the user was authenticated.
	 * @param time
	 */
	public void setUserSessionSetTime(final long time)
	{
		SharedPreferences.Editor editor = mUserPreferences.edit();
		editor.putLong(USER_PREFERENCE_SESSION_SET_TIME, time);
	}
	
	
	/**
	 * Set the Session ID for the user.
	 * @param Id
	 */
	public void setUserSessionId(final String Id)
	{
		SharedPreferences.Editor editor = mUserPreferences.edit();
		
		editor.putString(USER_PREFERENCE_SESSION_ID, Id);
		
		if(!editor.commit())
		{
			Log.e(TAG,"User Session Id could not be saved to preferences.");
		}
	}
	
	
	/**
	 * Saves the last location to the sharedPreferences
	 * @param time
	 * @param longitude
	 * @param latitude
	 */
	public void setLastLocation(final Location location)
	{
		SharedPreferences preferences = context.getSharedPreferences(LOCATION_PREFERENCE_FILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		
		editor.putLong(CACHE_KEY_LOCATION_TIME_SAVED, location.getTime());
		editor.putLong(CACHE_KEY_LOCATION_LONGITUDE, Double.doubleToLongBits(location.getLongitude()));
		editor.putLong(CACHE_KEY_LOCATION_LATITUDE, Double.doubleToLongBits(location.getLatitude()));
		editor.commit();
	}
	
	/**
	 * Returns Last Location saved in PreferenceManager
	 * @return
	 */
	public Location getLastLocation()
	{
		SharedPreferences preferences = context.getSharedPreferences(LOCATION_PREFERENCE_FILE, Context.MODE_PRIVATE);
		Location location = new Location(LocationHandler.MAIN_PROVIDER);
		location.setTime(preferences.getLong(CACHE_KEY_LOCATION_TIME_SAVED, LOCATION_NOT_SET));
		if(!preferences.contains(CACHE_KEY_LOCATION_LONGITUDE))
		{
			location.setLongitude(LOCATION_NOT_SET);
		}
		else
		{
			long bits =  preferences.getLong(CACHE_KEY_LOCATION_LONGITUDE, LOCATION_NOT_SET);

			location.setLongitude(Double.longBitsToDouble(bits));
		}
		
		if(!preferences.contains(CACHE_KEY_LOCATION_LATITUDE))
		{
			location.setLatitude(LOCATION_NOT_SET);
		}
		else
		{
			long bits =  preferences.getLong(CACHE_KEY_LOCATION_LATITUDE, LOCATION_NOT_SET);
			
			location.setLatitude(Double.longBitsToDouble(bits));
		}
		
		return location;
	}

	
	public long getLastPlaceSavedTime()
	{
		SharedPreferences preferences = context.getSharedPreferences(PLACE_PREFERENCE_FILE, Context.MODE_PRIVATE);
		return preferences.getLong(PLACE_KEY_TIME_SAVED, -1);
	}
	
	public void clearLastPlaceSaved()
	{
		SharedPreferences preferences = context.getSharedPreferences(PLACE_PREFERENCE_FILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(PLACE_KEY_TIME_SAVED, -1);
		editor.commit();
	}
	
	public void setLastPlaceSavedTime()
	{
		SharedPreferences preferences = context.getSharedPreferences(PLACE_PREFERENCE_FILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(PLACE_KEY_TIME_SAVED, System.currentTimeMillis());
		editor.commit();
	}
	
	public long getLastCommentSavedTime(final String placeId)
	{
		//return 0;
		SharedPreferences preferences = context.getSharedPreferences(COMMENT_PREFERENCE_FILE, Context.MODE_PRIVATE);
		
		return preferences.getLong(CACHE_KEY_COMMENTS+placeId, -1);
	}
	
	public void clearrAllCommentsSavedTimes()
	{
		SharedPreferences preferences = context.getSharedPreferences(COMMENT_PREFERENCE_FILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
	}
	
	public void setLastCommentsSavedTime(final String placeId)
	{
		SharedPreferences preferences = context.getSharedPreferences(COMMENT_PREFERENCE_FILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(CACHE_KEY_COMMENTS+placeId, System.currentTimeMillis());
		editor.commit();
		
	}
	
	
}
