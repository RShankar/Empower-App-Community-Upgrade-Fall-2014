package edu.fau.communityupgrade.callback;

public interface DefaultSaveCallback<T> {

	void onSaveComplete(T arg0);
	void onProviderNotAvailable();
	void onError(String error);
}
