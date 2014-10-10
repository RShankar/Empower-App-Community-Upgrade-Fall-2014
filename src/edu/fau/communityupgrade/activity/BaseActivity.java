package edu.fau.communityupgrade.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import edu.fau.communityupgrade.callback.UserLoginCallback;
import edu.fau.communityupgrade.database.UserManager;
import edu.fau.communityupgrade.preferences.ApplicationPreferenceManager;


/**
 * This serves as the Base Activity for all Application Activities to inherit, 
 * other than the user login and user sign up pages.
 * @author kyle
 *
 */
public class BaseActivity extends ActionBarActivity {

	//Used to handle all of the information about the user
	private final UserManager mUserManager = UserManager.getInstance();
	private static final String TAG = "BaseActivity";
	
	
	/**
	 * Used to check if the current user should be logged in.
	 */
	public void authenticateUser()
	{	
		//User has been logged in already
		if(mUserManager.isCurrentUserLoggedIn())
		{
			return;
		}
		//Get Preference Manager
		ApplicationPreferenceManager preferenceManager = 
				new ApplicationPreferenceManager(this);
		
		//Get SessionID for user
		String userSessionId = preferenceManager.getUserSessionId();
		
		//Callback to login the user
		UserLoginCallback loginCallBack = new UserLoginCallback(){

			@Override
			public void onSuccess(String userToken) {
			}

			@Override
			public void onFailure() {
				
				Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
				startActivity(intent);
			}

			@Override
			public void onError() {
				Log.d(TAG,"There was an error logging the user in.");
				finish();	
			}
		};
		
		
		
		if(userSessionId == null)
			loginCallBack.onFailure();
		else{
			mUserManager.Login(userSessionId, loginCallBack);	
		}
		
	}
	
}
