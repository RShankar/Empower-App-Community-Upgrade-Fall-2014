/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.fau.communityupgrade.maps;

import java.util.ArrayList;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.fau.communityupgrade.R;
import edu.fau.communityupgrade.activity.BaseActivity;
import edu.fau.communityupgrade.callback.DefaultFindCallback;
import edu.fau.communityupgrade.callback.LocationHandlerCallback;
import edu.fau.communityupgrade.database.PlaceManager;
import edu.fau.communityupgrade.location.LocationHandler;
import edu.fau.communityupgrade.models.Place;
import edu.fau.communityupgrade.ui.LoadingDialog;

/**
 * This shows how to create a simple activity with a map and a marker on the map.
 * <p>
 * Notice how we deal with the possibility that the Google Play services APK is not
 * installed/enabled/updated on a user's device.
 */
public class mapActivity extends BaseActivity 
		implements
		ConnectionCallbacks,
		OnConnectionFailedListener,
		LocationListener,
		OnMyLocationButtonClickListener, android.location.LocationListener{
    /**
     * Note that this may be null if the Google Play services APK is not available.
     */
    private GoogleMap mMap;
    
    private GoogleApiClient mGoogleApiClient;
   // private TextView mMessageView;
    
    
    //LocationManager test
    //private LocationManager locationManager;
    private PlaceManager placeManager;
    private ArrayList<Place> places;
    private LoadingDialog loadingDialog;
    private LocationHandler locationHandler;
    
    
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    
    // These settings are the same as the settings for the map. They will in fact give you updates
    // at the maximal rates currently possible.
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	getActionBar().setHomeButtonEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        setUpMapIfNeeded();
        placeManager = new PlaceManager(this);
        loadingDialog = new LoadingDialog(this);
        locationHandler = new LocationHandler(this);
        //Variables for LocationManager Test
        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        setUpGoogleApiClientIfNeeded();
        mGoogleApiClient.connect();
        
        
        locationHandler.updateLocation(new LocationHandlerCallback(){

			@Override
			public void onLocationUpdate(Location location) {
				onLocationChanged(location);
				
			}

			@Override
			public void onProviderNotAvailable() {
				// TODO Auto-generated method stub
				
			}
        	
        	
        });
        
        loadingDialog.show();
        placeManager.getAllPlacesNearUser(50.0, new DefaultFindCallback<Place>(){

			@Override
			public void onComplete(ArrayList<Place> list) {
				loadingDialog.dismiss();
				places = list;
				setMarkers();
			}

			@Override
			public void onProviderNotAvailable() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onError(String error) {
				// TODO Auto-generated method stub
				
			}
        	
        	
        	
        });
    }
    
    public void setMarkers()
    {
    	for(int i=0;i<places.size();i++)
    	{
    		Place place = places.get(i);
    		LatLng lt = new LatLng(place.getLatitude(),place.getLongitude());
    		mMap.addMarker(new MarkerOptions().position(lt)
    				.title(place.getName())
    				.snippet(place.getName()+": "+place.getDescription()+", created by "+place.getCreatedBy().getUsername())
    				);
    	}
    	
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                //setUpMap(); replaced by the following
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(this);
               // mMap.animateCamera(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
               // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.87365, 151.20689), 10));
            }
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }
    
    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     * Replaced by mMap.setMyLocationEnabled(true); & 
     * mMap.setOnMyLocationButtonClickListener(this);
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        
    }
    */
    
    private void setUpGoogleApiClientIfNeeded() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }
    /**
     * Button to get current Location. This demonstrates how to get the current Location as required
     * without needing to register a LocationListener.
     */
    public void showMyLocation(View view) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            String msg = "Location = "
                    + LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }
    } 
    
    /** commented function because its called at the bottom for LocationManger test
     * Implementation of {@link LocationListener}.
     
    @Override
    public void onLocationChanged(Location location) {
        //mMessageView.setText("Location = " + location);
    }
     */
    /**
     * Callback called when connected to GCore. Implementation of {@link ConnectionCallbacks}.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                REQUEST,
                this);  // LocationListener
    }

    /**
     * Callback called when disconnected from GCore. Implementation of {@link ConnectionCallbacks}.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        // Do nothing
    }

    /**
     * Implementation of {@link OnConnectionFailedListener}.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Do nothing
    }
    
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
    
    //New test for camera update
    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        mMap.animateCamera(cameraUpdate);
       // locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }
    
    

}




