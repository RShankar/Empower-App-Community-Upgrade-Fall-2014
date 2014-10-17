package edu.fau.communityupgrade.callback;

public interface DefaultSaveCallback<T> {

	void onSaveComplete(T arg0);
	void onError(String error);
}
