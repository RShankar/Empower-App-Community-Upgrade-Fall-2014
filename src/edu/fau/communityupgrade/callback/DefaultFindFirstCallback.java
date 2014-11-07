package edu.fau.communityupgrade.callback;


public interface DefaultFindFirstCallback<T> {

	void onComplete(final T object);
	
	void onProviderNotAvailable();
	
	void onError(String error);
}
