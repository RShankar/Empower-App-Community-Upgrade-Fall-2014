package edu.fau.communityupgrade.callback;

//This callback is to be used by the Activity to get information about the 
//result of the login
//
public interface UserLoginCallback {

	//This function is called when the user has successfully signed up
	//The session token is given to set for future use
	void onSuccess(String userToken);
	
	//This function is called when the user has failed to sign up
	void onFailure();
	
	//This is called when the there was an error in the process
	void onError();
}
