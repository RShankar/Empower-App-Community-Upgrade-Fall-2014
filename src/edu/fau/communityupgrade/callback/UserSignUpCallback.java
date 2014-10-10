package edu.fau.communityupgrade.callback;

//This callback is to be used when attempts to sign up
public interface UserSignUpCallback {

	//This function is called when the user has successfully signed up
	//The session token is given to set for future use
	void onSuccess(String userToken);
	
	//This function is called when the user has failed to sign up
	void onFailure();
}
