package edu.fau.communityupgrade.activity;

import android.support.v7.app.ActionBarActivity;
import edu.fau.communityupgrade.auth.Auth;
import edu.fau.communityupgrade.database.UserManager;


/**
 * This serves as the Base Activity for all Application Activities to inherit, 
 * other than the user login and user sign up pages.
 * @author kyle
 */
public class BaseActivity extends ActionBarActivity {

	//Used to handle all of the information about the user
	private final UserManager mUserManager = UserManager.getInstance();
	private static final String TAG = "BaseActivity";
	private Auth auth;
	
	@Override
	public void onResume()
	{
		super.onResume();
		auth = new Auth(this);
		auth.authenticateUser();
	}
	
}
