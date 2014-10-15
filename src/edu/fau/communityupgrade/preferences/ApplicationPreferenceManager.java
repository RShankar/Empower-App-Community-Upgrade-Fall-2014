package edu.fau.communityupgrade.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * This is a class to handle all of the preferences in the 
 * SharedPreferences for the application.
 * @author kyle
 *
 */
public class ApplicationPreferenceManager {

	private static final String USER_PREFERENCE_FILE = "UserPreferences"; 
	private static final String USER_PREFERENCE_SESSION_ID = "UserSessionID";
	private static final String USER_PREFERENCE_SESSION_SET_TIME = "UserSessionSetTime";
	public static final long USER_PREFERENCE_EMPTY_SESSION_TIME = -1;
	
	private static final String TAG = "ApplicationPreferenceManager";
	
	private final Context context;
	private SharedPreferences mUserPreferences;
	
	public ApplicationPreferenceManager(Context context)
	{
		this.context = context;
		mUserPreferences = context.getSharedPreferences(
				USER_PREFERENCE_FILE, Context.MODE_PRIVATE);;
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
	
}
