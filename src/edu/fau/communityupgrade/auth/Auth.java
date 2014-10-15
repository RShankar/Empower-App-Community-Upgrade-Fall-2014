package edu.fau.communityupgrade.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.util.Log;
import edu.fau.communityupgrade.activity.LoginActivity;
import edu.fau.communityupgrade.callback.UserLoginCallback;
import edu.fau.communityupgrade.database.UserManager;
import edu.fau.communityupgrade.preferences.ApplicationPreferenceManager;

public class Auth {

	private UserManager mUserManager;
	private Context context;
	private static final String TAG = "Auth";
	
	private static final long CHECK_AUTH_RATE_MILLI = 30000*30;
	
	public Auth(final Context context)
	{
		this.context = context;
		mUserManager = UserManager.getInstance();
	}
	
	/**
	 * Used to check if the current user should be logged in.
	 */
	public void authenticateUser()
	{	
		//Get current time
		Time time = new Time();
		time.setToNow();
		
		//Get Preference Manager
		ApplicationPreferenceManager preferenceManager = 
				new ApplicationPreferenceManager(context);
		
		long lastUserAuthTime = preferenceManager.getUserSessionSetTime();
		
		if(lastUserAuthTime == ApplicationPreferenceManager.USER_PREFERENCE_EMPTY_SESSION_TIME
			|| lastUserAuthTime > time.toMillis(true))
		{
			//Set the next time the user should be authenticated
			preferenceManager.setUserSessionSetTime(time.toMillis(true)+CHECK_AUTH_RATE_MILLI);
		}
		else
		{
			//USer has already been authenticated
			return;
		}
		
		
		//Get SessionID for user
		String userSessionId = preferenceManager.getUserSessionId();
		
		//Callback to login the user
		UserLoginCallback loginCallBack = new UserLoginCallback(){

			@Override
			public void onSuccess(String userToken) {
			}

			@Override
			public void onFailure() {
				//Go to Login Page
				Intent intent = new Intent(context.getApplicationContext(),
						LoginActivity.class);
				context.startActivity(intent);
			}

			@Override
			public void onError() {
				Log.e(TAG,"There was an error logging the user in.");
				((Activity) context).finish();	
			}
		};
		
		
		
		if(userSessionId == null)
			loginCallBack.onFailure();
		else{
			mUserManager.Login(userSessionId, loginCallBack);	
		}
		
	}
}
