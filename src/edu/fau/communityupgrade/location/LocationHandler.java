package edu.fau.communityupgrade.location;

import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import edu.fau.communityupgrade.callback.LocationHandlerCallback;

/**
 * This class handles the location components of the application.
 * @author kyle
 *
 */
public class LocationHandler {

	private final Context mContext;
	private final LocationManager mLocationManager;
	private final Criteria mCriteria;
	private final static String TAG = "LocationHandler";
	private final LocationHandlerCallback mLocationHandlerCallback;
	
	private static final String MAIN_PROVIDER =  LocationManager.NETWORK_PROVIDER;
	
	public LocationHandler(Context context, LocationHandlerCallback callback)
	{
		mContext = context;
		mLocationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
		mLocationHandlerCallback = callback;
		mCriteria = new Criteria();
		
		mCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
		updateLocation();
	}
	
	//Update Location
	public void updateLocation()
	{
		if(!mLocationManager.isProviderEnabled(MAIN_PROVIDER))
		{
			mLocationHandlerCallback.onProviderNotAvailable();
		}
		
		mLocationManager.requestSingleUpdate(MAIN_PROVIDER, 
				new LocationHandlerListener(), mContext.getMainLooper());
		
		
	}
	
	private class LocationHandlerListener implements LocationListener
	{

		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG,"Location changed");
			mLocationHandlerCallback.onLocationUpdate(location);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderDisabled(String provider) {
			
			
		}
		
	}
}
