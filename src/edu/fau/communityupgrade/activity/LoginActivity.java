package edu.fau.communityupgrade.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.fau.communityupgrade.R;
import edu.fau.communityupgrade.auth.Auth;
import edu.fau.communityupgrade.callback.AuthCallback;
import edu.fau.communityupgrade.callback.UserLoginCallback;
import edu.fau.communityupgrade.database.UserManager;
import edu.fau.communityupgrade.preferences.ApplicationPreferenceManager;
import edu.fau.communityupgrade.ui.LoadingDialog;

public class LoginActivity extends Activity {

	private EditText username;
	private EditText password;
	private UserManager mUserManager;
	private TextView signUp;
	private Button loginBtn;
	
	//Progress Dialog to show when user is being logged in
	private LoadingDialog mProgressDialog;
	
	private static final String TAG = "SignUpActivity";
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG,"onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		//Setup Views as objects
		username = (EditText)findViewById(R.id.login_username);
		password = (EditText)findViewById(R.id.login_password);
		signUp = (TextView)findViewById(R.id.login_to_signup_page);
		loginBtn = (Button)findViewById(R.id.login_button);
		
		//Intialize UserManager
		mUserManager = UserManager.getInstance();
		
		//Set OnClickListener for Login Button
		loginBtn.setOnClickListener(new LoginListener());
		
		//Progress Dialog to show the user logging in.
		mProgressDialog = new LoadingDialog(this,"Logging in","Logging User in..");
		
		//set OnClick Listener for signUp button
		signUp.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
				startActivity(intent);
			}
			
			
		});
	}
	
	@Override
	public void onResume()
	{
		Log.d(TAG,"onResume");
		super.onResume();
		mProgressDialog.show();
		Auth auth = new Auth(this);
		auth.authenticateUser(new AuthCallback(){
			@Override
			public void onAuthenticationFailure() {
				mProgressDialog.dismiss();
			}

			@Override
			public void onAuthenticationSuccess() {
				mProgressDialog.dismiss();
				GoToMainPage();
				
			}
			
		});
	}
	
	private void GoToMainPage()
	{
		String packageName = this.getPackageName();
		Intent intent = this.getPackageManager().getLaunchIntentForPackage(packageName);
		
		startActivity(intent);
	}
	
	/**
	 * This is a private class to handle logging in when the user
	 * clicks the Login Button
	 * @author kyle
	 */
	private class LoginListener implements OnClickListener
	{
		@Override
		public void onClick(View v) {
			
			mProgressDialog.show();
			
			mUserManager.Login(username.getText().toString(), 
					password.getText().toString(),
					new UserLoginCallback(){

						@Override
						public void onSuccess(String userToken) {
							
							ApplicationPreferenceManager preferenceManager = 
									new ApplicationPreferenceManager(LoginActivity.this);
							
							preferenceManager.setUserSessionId(userToken);
							GoToMainPage();
						}

						@Override
						public void onFailure() {
							mProgressDialog.dismiss();
							Toast.makeText(getApplicationContext(), 
									"Incorrect Username, Password combination.", 
									Toast.LENGTH_LONG).show();
							
						}

						@Override
						public void onError() {
							mProgressDialog.dismiss();
							Toast.makeText(getApplicationContext(), 
									"There was an error logging you in. " +
									"Please try again later.", 
									Toast.LENGTH_LONG).show();
							
						}
				
				
			});
		}	
	}
	
}
