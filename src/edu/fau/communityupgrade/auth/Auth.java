package edu.fau.communityupgrade.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.util.Log;
import edu.fau.communityupgrade.activity.LoginActivity;
import edu.fau.communityupgrade.callback.AuthCallback;
import edu.fau.communityupgrade.callback.UserLoginCallback;
import edu.fau.communityupgrade.database.UserManager;
import edu.fau.communityupgrade.preferences.ApplicationPreferenceManager;

public class Auth {

	private UserManager mUserManager;
	private Context context;
	private static final String TAG = "Auth";
	private final ApplicationPreferenceManager preferenceManager;
	
	private static final long CHECK_AUTH_RATE_MILLI = 15000;
	
	public Auth(final Context context)
	{
		this.context = context;
		mUserManager = UserManager.getInstance();
		preferenceManager = new ApplicationPreferenceManager(context);
	}
	
	public boolean isUserAuthenticationExpired()
	{
		//Last time the user was authenticated
		long lastUserAuthTime = preferenceManager.getUserSessionSetTime();
		
		//Get current time
		Time time = new Time();
		time.setToNow();
		
		if(lastUserAuthTime == ApplicationPreferenceManager.USER_PREFERENCE_EMPTY_SESSION_TIME
			|| lastUserAuthTime > time.toMillis(true))
		{
			return true;
		}
		else
		{
			//USer has already been authenticated
			return false;
		}
	}
	
	
	/**
	 * Authenticates current User using Session ID stored 
	 * in preference manager.
	 */
	public void authenticateUser(final AuthCallback callback)
	{	
		//Get SessionID for user
		String userSessionId = preferenceManager.getUserSessionId();
		
		//Callback to login the user
		UserLoginCallback loginCallBack = new UserLoginCallback(){
			@Override
			public void onSuccess(String userToken) {
				callback.onAuthenticationSuccess();
			}

			@Override
			public void onFailure() {
				
				callback.onAuthenticationFailure();
			}

			@Override
			public void onError() {
				Log.e(TAG,"There was an error logging the user in.");
				callback.onAuthenticationFailure();
				((Activity) context).finish();	
			}
		};
		
		
		
		if(userSessionId == null)
			loginCallBack.onFailure();
		else{
			mUserManager.Login(userSessionId, loginCallBack);	
		}
		
	}
	
	public void logout()
	{
		preferenceManager.removeUserSession();
		mUserManager.Logout(context);
		
	}
}
