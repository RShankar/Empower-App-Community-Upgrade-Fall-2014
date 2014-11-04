package edu.fau.communityupgrade.location;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import edu.fau.communityupgrade.callback.LocationHandlerCallback;
import edu.fau.communityupgrade.preferences.ApplicationPreferenceManager;

/**
 * This class handles the location components of the application.
 * @author kyle
 *
 */
public class LocationHandler {

	private final Context mContext;
	private final LocationManager mLocationManager;
	private final Criteria mCriteria;
	private final ApplicationPreferenceManager preferenceManager;
	
	private final static String TAG = "LocationHandler";
	
	private static final long CACHE_TIME_EXPIRED = 5000;
	
	private static final int MAX_LOCATIONS_FROM_GEOCODER = 1;
	
	public static final String MAIN_PROVIDER =  LocationManager.NETWORK_PROVIDER;
	
	private static final String[] PROVIDERS = {LocationManager.GPS_PROVIDER,
												LocationManager.NETWORK_PROVIDER
												};
	
	public LocationHandler(Context context)
	{
		mContext = context;
		mLocationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
		preferenceManager = new ApplicationPreferenceManager(mContext);
		
		mCriteria = new Criteria();
		mCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
	}
	
	private boolean isCacheExpired()
	{
		ApplicationPreferenceManager preferenceManager = new ApplicationPreferenceManager(mContext);
		final long lastSavedTime = preferenceManager.getLastLocation().getTime();
		final long currentTime = System.currentTimeMillis();
		
		if(lastSavedTime == ApplicationPreferenceManager.LOCATION_NOT_SET)
		{
			return true;
		}
		if(currentTime - lastSavedTime > CACHE_TIME_EXPIRED)
		{
			return true;
		}
		else
		{
			return false;
		}
		
	}
	
	//Update Location
	public void updateLocation(LocationHandlerCallback callback)
	{
		if(!isCacheExpired())
		{
			Location location = preferenceManager.getLastLocation();
			callback.onLocationUpdate(location);
		}
		else if(mLocationManager.isProviderEnabled(MAIN_PROVIDER))
		{
			mLocationManager.requestSingleUpdate(MAIN_PROVIDER, 
					new LocationHandlerListener(callback), mContext.getMainLooper());
			return;
		}
		
		for(int i=0;i<PROVIDERS.length;i++)
		{
			if(mLocationManager.isProviderEnabled(PROVIDERS[i]))
			{
				Log.d(TAG,"Location Provider Available");
				mLocationManager.requestSingleUpdate(PROVIDERS[i], 
						new LocationHandlerListener(callback),null);
				return;
			}
		}
		
		callback.onProviderNotAvailable();	
	}
	
	public Location getLocationFromAddress(final String address)
	{
	    Geocoder coder = new Geocoder(mContext);
	    try {
	        ArrayList<Address> adresses = (ArrayList<Address>)coder
	        		.getFromLocationName(address, MAX_LOCATIONS_FROM_GEOCODER);
	        
        	Location location = new Location(LocationManager.NETWORK_PROVIDER);

	        for(Address add : adresses){
	        	double longitude = add.getLongitude();
	            double latitude = add.getLatitude();
	        	location.setLongitude(longitude);
	            location.setLatitude(latitude);
	        }
	        
	        return location;
	    } catch (IOException e) {
	    	Log.e(TAG,"Error get Location from Address.");
	        e.printStackTrace();
	        return null;
	    }
	}
	
	private class LocationHandlerListener implements LocationListener
	{

		private final LocationHandlerCallback mLocationHandlerCallback;
		
		public LocationHandlerListener(LocationHandlerCallback callback)
		{
			mLocationHandlerCallback = callback;
		}
		
		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG,"Location changed");
			preferenceManager.setLastLocation(location);
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
