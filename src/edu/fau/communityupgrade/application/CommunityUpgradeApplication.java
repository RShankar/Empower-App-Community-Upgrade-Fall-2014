package edu.fau.communityupgrade.application;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;

import edu.fau.communityupgrade.R;

public class CommunityUpgradeApplication extends Application {

	@Override
	public void onCreate()
	{
		super.onCreate();
		
		//Initializes Parse
		Parse.initialize(this, 
				getString(R.string.application_id), 
				getString(R.string.client_key));
		
		ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
 
        // If you would like all objects to be private by default, remove this
        // line.
        defaultACL.setPublicReadAccess(true);
 
        ParseACL.setDefaultACL(defaultACL, true);
				
	}
}


