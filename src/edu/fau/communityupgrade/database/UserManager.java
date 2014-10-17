package edu.fau.communityupgrade.database;

import android.util.Log;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import edu.fau.communityupgrade.callback.UserLoginCallback;
import edu.fau.communityupgrade.callback.UserSignUpCallback;
import edu.fau.communityupgrade.helper.ParseHelper;
import edu.fau.communityupgrade.models.User;


/**
 * This Singleton class connects to the Parse Database
 * and handles all of the connections for the current user.
 * @author kyle
 *
 */
public class UserManager {

	static final String TAG = "UserManager";
	
	public static final String TABLE = "_User";
	public static final String USERNAME = "username";
	private volatile boolean isLoggedIn = false;
	final ParseUser currentUser;
	private static UserManager mUserManager;
	
	private UserManager()
	{
		currentUser = new ParseUser();
	}
	
	public static UserManager getInstance()
	{
		if(mUserManager == null)
		{
			mUserManager = new UserManager();
		}
		
		return mUserManager;

	}
	
	/**
	 * Login the user using a username, password combo
	 * @param username
	 * @param password
	 * @param callback UserSignUpCallback to be called after user is attempte to login
	 */
	public void Login(String username, String password, UserLoginCallback callback)
	{
		try
		{	
			ParseUser user = ParseUser.logIn(username, password);
			callback.onSuccess(user.getSessionToken());
		}
		catch(ParseException e)
		{
			Log.e(TAG,"Error logging in: ",e);
			callback.onFailure();
		}
	}

	/**
	 * Login the user using a session token
	 * @param sessionToken
	 * @param callback
	 */
	public void Login(String sessionToken, UserLoginCallback callback)
	{
		try
		{	
			ParseUser user = ParseUser.become(sessionToken);
			callback.onSuccess(user.getSessionToken());
		}
		catch(ParseException e)
		{
			Log.e(TAG,"Error logging in: ",e);
			callback.onFailure();
		}
		
	}
	
	/**
	 * Returns current User as User Object.
	 * @return 
	 */
	public User getCurrentUser()
	{
		return ParseHelper.parseUserToUser(ParseUser.getCurrentUser());
	}
	
	
	
	/**
	 * Signs the user up and logs the user in
	 * @param username
	 * @param password
	 * @param callback
	 * @return
	 */
	public void SignUp(String username, String password, final UserSignUpCallback callback)
	{
		currentUser.setUsername(username);
		currentUser.setPassword(password);
		ParseUserSignupCallBack call = new ParseUserSignupCallBack(callback);
		try
		{
			currentUser.signUp();
		}
		catch(ParseException e)
		{
			call.done(e);
		}
	}
	
	
	/**
	 * Returns the current user as a ParseUser object.
	 * Available only to classes within same package.
	 * @return
	 */
	protected ParseUser getParseUser()
	{
		return ParseUser.getCurrentUser();
	}
	
	/**
	 * Implementation of Parse's SignUpCallback
	 * Requires UserLoginCallback object which is used to send results
	 * back to Activity
	 * @author kyle
	 */
	private class ParseUserSignupCallBack extends SignUpCallback
	{
		private final UserSignUpCallback callback;
		
		public ParseUserSignupCallBack(UserSignUpCallback c)
		{
			callback = c;
		}
		
		@Override
		public void done(ParseException e) {
			if(e==null)
			{
				isLoggedIn = true;
				callback.onSuccess(ParseUser.getCurrentUser().getSessionToken());
			}
			else
			{
				Log.e(TAG,"Error Signing User Up: ",e);
				callback.onFailure();
			}
		}
	}
	
}
