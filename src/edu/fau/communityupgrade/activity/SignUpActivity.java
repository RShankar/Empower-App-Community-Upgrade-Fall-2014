package edu.fau.communityupgrade.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import edu.fau.communityupgrade.R;
import edu.fau.communityupgrade.callback.UserSignUpCallback;
import edu.fau.communityupgrade.database.UserManager;
import edu.fau.communityupgrade.preferences.ApplicationPreferenceManager;
import edu.fau.communityupgrade.ui.LoadingDialog;

public class SignUpActivity extends Activity {

	private EditText username;
	private EditText password;
	private UserManager mUserManager;
	private Button signupBtn;
	private LoadingDialog mProgressDialog;
	
	
	private static final String TAG = "SignUpActivity";
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup);
		
		username = (EditText)findViewById(R.id.signup_username);
		password = (EditText)findViewById(R.id.signup_password);
		signupBtn = (Button)findViewById(R.id.signup_button);
		mUserManager = UserManager.getInstance();
		signupBtn.setOnClickListener(new SignUpListener());
		mProgressDialog = new LoadingDialog(this);
	}
	
	@Override
	public void onResume()
	{
		Log.d(TAG,"onResume");
		super.onResume();
	}
	
	private boolean validateInformation()
	{
		return (!username.getText().toString().isEmpty() ||
				!password.getText().toString().isEmpty());
		
		
	}

	/**
	 * This private class is used to handle the signing up of a user 
	 * after the Sign Up Button has been pressed.
	 * @author kyle
	 */
	private class SignUpListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(!validateInformation()){
				Toast.makeText(getApplicationContext(), 
						getString(R.string.signup_validation_error), 
						Toast.LENGTH_LONG).show();
				
				return;
			}
			
			mProgressDialog.show();
			Log.d(TAG,"username: "+username.getText().toString()+", pass: "+password.getText());
			
			
			mUserManager.SignUp(username.getText().toString(), password.getText().toString(),
					new UserSignUpCallback(){

						@Override
						public void onSuccess(String userToken) {
							ApplicationPreferenceManager preferenceManager = 
									new ApplicationPreferenceManager(SignUpActivity.this);
							preferenceManager.setUserSessionId(userToken);
							
							PackageManager pm = getPackageManager();
							Intent launchIntent = pm.getLaunchIntentForPackage(
									getApplicationContext().getPackageName());
							finish();
							startActivity(launchIntent);
						}

						@Override
						public void onFailure() {
							mProgressDialog.dismiss();
							Toast.makeText(getApplicationContext(), 
									getString(R.string.signup_error), 
									Toast.LENGTH_LONG).show();
								
						}
			});
		
		}
		
		
	}
}


