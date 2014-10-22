package edu.fau.communityupgrade.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.WindowManager;
import edu.fau.communityupgrade.auth.Auth;
import edu.fau.communityupgrade.callback.AuthCallback;


/**
 * This serves as the Base Activity for all Application Activities to inherit, 
 * other than the user login and user sign up pages.
 * @author kyle
 */
public class BaseActivity extends ActionBarActivity {

	private static final String TAG = "BaseActivity";
	private boolean firstRun = true;
	private Auth auth;
	
	@Override
	public void onResume()
	{
		super.onResume();
		
	    
		
		Log.d(TAG,"onResume");
		auth = new Auth(this);
		if(firstRun || auth.isUserAuthenticationExpired()){
			firstRun = false;
			auth.authenticateUser(new BaseAuthCallback());
		}
	}
	
	private class BaseAuthCallback implements AuthCallback
	{

		@Override
		public void onAuthenticationFailure() {
			
			//Go to Login Page
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(intent);
		}

		@Override
		public void onAuthenticationSuccess() {
		}
		
		
	}
	
}
