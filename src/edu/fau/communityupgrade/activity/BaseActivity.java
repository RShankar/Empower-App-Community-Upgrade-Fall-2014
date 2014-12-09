package edu.fau.communityupgrade.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import edu.fau.communityupgrade.R;
import edu.fau.communityupgrade.auth.Auth;
import edu.fau.communityupgrade.callback.AuthCallback;


/**
 * This serves as the Base Activity for all Application Activities to inherit, 
 * other than the user login and user sign up pages.
 * @author kyle
 */
public class BaseActivity extends FragmentActivity {

	private static final String TAG = "BaseActivity";
	
	protected static final String MAIN_COLOR = "MAIN_COLOR",
								MAIN_COLOR_DEFAULT = "#5fc5f0",
								RADIUS_IN_MILES = "RADIUS_IN_MILES",
								APPLICATION_NAME = "APPLICATION_NAME",
								APPLICATION_NAME_DEFAULT = "Community Upgrade",
								MAP_PAGE_TITLE = "MAP_PAGE_TITLE",
								MAP_PAGE_TITLE_DEFAULT = "Places Map";
	
	protected static final int RADIUS_IN_MILES_DEFAULT = 45;
	private ActionBar actionBar;
	
	private boolean firstRun = true;
	private Auth auth;
	
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		
		/* Retrieve Extras from Processing */
		Intent intent = getIntent();
		
		if(intent.getExtras() != null){
			Bundle extras = intent.getExtras();
			
			
			final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			Editor editor = preferences.edit();
			
			if(extras.containsKey(MAIN_COLOR)){
				String mainColor = extras.getString(MAIN_COLOR);
				editor.putString(MAIN_COLOR, mainColor);
			}
			
			
			if(extras.containsKey(RADIUS_IN_MILES)){
				int radiusInMiles = extras.getInt(RADIUS_IN_MILES);
				editor.putInt(RADIUS_IN_MILES, radiusInMiles);
			}
			
			if(extras.containsKey(MAP_PAGE_TITLE))
			{
				String mapTitle = extras.getString(MAP_PAGE_TITLE);
				editor.putString(MAP_PAGE_TITLE,mapTitle);
			}
			editor.commit();
		}
		
		updateActionBar();
		
		auth = new Auth(this);
		if(firstRun || auth.isUserAuthenticationExpired()){
			firstRun = false;
			auth.authenticateUser(new BaseAuthCallback());
		}
	}
	
	private void updateActionBar()
	{
		actionBar = getActionBar();
		actionBar.setLogo( R.drawable.icon );
		actionBar.setHomeButtonEnabled(true);
		
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); 
		String color = preferences.getString(MAIN_COLOR, MAIN_COLOR_DEFAULT);
		String appName = preferences.getString(APPLICATION_NAME, APPLICATION_NAME_DEFAULT);
		Log.d(TAG,"Color: "+color);
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(color)));
		actionBar.setTitle(appName);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    onBackPressed();
	    return true;
	}
	
	protected void logout()
	{
		auth.logout();
		Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
		startActivity(intent);
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
			finish();
			startActivity(intent);
		}

		@Override
		public void onAuthenticationSuccess() {
		}
		
		
	}
	
}
