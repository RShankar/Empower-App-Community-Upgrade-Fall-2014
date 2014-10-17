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
	
	private static final int MAX_LOCATIONS_FROM_GEOCODER = 1;
	
	private static final String MAIN_PROVIDER =  LocationManager.NETWORK_PROVIDER;
	
	private static final String[] PROVIDERS = {LocationManager.NETWORK_PROVIDER,
												};
	
	public LocationHandler(Context context)
	{
		mContext = context;
		mLocationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);

		mCriteria = new Criteria();
		
		mCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
	}
	
	//Update Location
	public void updateLocation(LocationHandlerCallback callback)
	{
		
		for(int i=0;i<PROVIDERS.length;i++)
		{
			if(mLocationManager.isProviderEnabled(MAIN_PROVIDER))
			{
				mLocationManager.requestSingleUpdate(MAIN_PROVIDER, 
						new LocationHandlerListener(callback), mContext.getMainLooper());
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
