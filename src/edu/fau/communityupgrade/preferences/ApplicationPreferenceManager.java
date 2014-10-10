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
	private static final String TAG = "ApplicationPreferenceManager";
	
	private final Context context;
	
	public ApplicationPreferenceManager(Context context)
	{
		this.context = context;
	}
	
	/**
	 * Returns the sessionID stored in the user preferences.
	 *  If it does not exist, null is returned
	 * @return
	 */
	public String getUserSessionId()
	{
		SharedPreferences preferences = context.getSharedPreferences(
				USER_PREFERENCE_FILE, Context.MODE_PRIVATE);
		String sessionID = preferences.getString(USER_PREFERENCE_SESSION_ID, null);
		return sessionID;
	}
	
	/**
	 * Set the Session ID for the user.
	 * @param Id
	 */
	public void setUserSessionId(final String Id)
	{
		SharedPreferences preferences = context.getSharedPreferences(
				USER_PREFERENCE_FILE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		
		editor.putString(USER_PREFERENCE_SESSION_ID, Id);
		
		if(!editor.commit())
		{
			Log.e(TAG,"User Session Id could not be saved to preferences.");
		}
	}
	
}
