package edu.fau.communityupgrade.callback;

import android.location.Location;

public interface LocationHandlerCallback {

	public void onLocationUpdate(Location location);
	
	public void onProviderNotAvailable();
}
