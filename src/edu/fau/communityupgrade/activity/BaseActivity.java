package edu.fau.communityupgrade.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import edu.fau.communityupgrade.auth.Auth;
import edu.fau.communityupgrade.callback.AuthCallback;


/**
 * This serves as the Base Activity for all Application Activities to inherit, 
 * other than the user login and user sign up pages.
 * @author kyle
 */
public class BaseActivity extends FragmentActivity {

	private static final String TAG = "BaseActivity";
	private ActionBar actionBar;
	
	private boolean firstRun = true;
	private Auth auth;
	
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    onBackPressed();
	    return true;
	}
	
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
	
	/**
	 * Used start 
	 * @author kyle
	 *
	 */
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
