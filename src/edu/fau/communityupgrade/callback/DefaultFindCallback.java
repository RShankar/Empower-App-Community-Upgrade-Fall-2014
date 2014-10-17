package edu.fau.communityupgrade.callback;

import java.util.ArrayList;

/**
 * This Callback will be used as the default callback for all DB connections.
 * Anything retrieved from Parse is done so on a separate thread. When that 
 * has been completed, it will call this interface which will tie the information
 * back to the Activity.
 * @author kyle
 *
 */
public interface DefaultFindCallback<T> {

	void onComplete(ArrayList<T> list);
	
	void onError(String error);
}
