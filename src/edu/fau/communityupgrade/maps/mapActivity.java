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

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
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
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.fau.communityupgrade.R;
import edu.fau.communityupgrade.activity.BaseActivity;
import edu.fau.communityupgrade.activity.SinglePlaceActivity;
import edu.fau.communityupgrade.activity.TestPlaceActivity;
import edu.fau.communityupgrade.callback.DefaultFindCallback;
import edu.fau.communityupgrade.database.PlaceManager;
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
		OnMarkerClickListener,
		LocationListener,
		OnMyLocationButtonClickListener, android.location.LocationListener{
    /**
     * Note that this may be null if the Google Play services APK is not available.
     */
    private GoogleMap mMap;
    
    private GoogleApiClient mGoogleApiClient;
   // private TextView mMessageView;
    
    //Setting the marker displays
    private TextView mMarkerInfoTitle;
    private TextView mMarkerDescription;
    private TextView mMarkerCreator;
    //LocationManager test
    private LocationManager locationManager;
    private PlaceManager placeManager;
    private ArrayList<Place> places;
    private LoadingDialog loadingDialog;
    
    //private static final long MIN_TIME = 1000; //test changed to 4,000 from 400
    //private static final float MIN_DISTANCE = 1000;
    
    private static final String TAG = "mapActivity";
    
    // These settings are the same as the settings for the map. They will in fact give you updates
    // at the maximal rates currently possible.
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(50000)         // 50,000 = 50 seconds
            .setFastestInterval(25000)    // 25,000 means 25 secs for each location update
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	getActionBar().setHomeButtonEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        
        //setting the views for the marker display
        mMarkerInfoTitle = (TextView) findViewById(R.id.marker_info_title);
        mMarkerDescription = (TextView) findViewById(R.id.marker_description);
        mMarkerCreator = (TextView) findViewById(R.id.marker_id);
        
        mMarkerInfoTitle.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {

				Log.d(TAG,"mark_info_title: test");
				
				/*Get current Place
				Place place;
				place.
				this.objectId = objectId;
				this.name = name;
				this.createdBy = user;
				this.latitude = latitude;
				this.longitude = longitude;
				*/

				
				//Intent to start next activity
				Intent intent = new Intent(mapActivity.this,SinglePlaceActivity.class);
				for(int i=0;i<places.size();i++)
		    	{
					Log.d(TAG,"mark_info_title: test INSIDE LOOP");
					Place place = places.get(i);
					Log.d(TAG,"mark_info_title place ID = " + place.getObjectId() + "; Marker = " + mMarkerCreator.getText());
					LatLng latLng = new LatLng(place.getLatitude(),place.getLongitude());
					if( place.getName().equals(mMarkerInfoTitle.getText().toString()) && 
							latLng.toString().equals(mMarkerCreator.getText().toString()) )
					{
						Log.d(TAG,"mark_info_title: test INSIDE LOOP - IF!");
						//bundle to hold current Place
						Bundle mBundle = new Bundle();  
				        mBundle.putParcelable(SinglePlaceActivity.PLACE_OBJECT_EXTRA_KEY, place);  
						intent.putExtras(mBundle);
						startActivity(intent);
					}

		    	}
				
	
			}		
		});
        
        
        setUpMapIfNeeded();
        placeManager = new PlaceManager(this);
        loadingDialog = new LoadingDialog(this);
        
        //Variables for LocationManager Test locationManager is currently needed but
        //maybe because others are calling it when it isn't initialized
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        setUpGoogleApiClientIfNeeded();
        mGoogleApiClient.connect();
        findViewById(R.id.marker_title).setVisibility(View.INVISIBLE);
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
    				.snippet(place.getAddress()+", created by: "+place.getCreatedBy().getUsername())
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
                mMap.setOnMarkerClickListener(this);
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
    	Log.d(TAG,"onLocationChanged every 5 sec?");
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        mMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }
    
    @Override
    public boolean onMarkerClick(final Marker marker) {
    	findViewById(R.id.marker_title).setVisibility(View.VISIBLE);
    	
    	//Once a marker has been clicked a Display pops up
    	//This fill the displays
    	mMarkerInfoTitle.setText(marker.getTitle());
    	mMarkerDescription.setText(marker.getSnippet());
    	mMarkerCreator.setText(marker.getPosition().toString());
		return false;
    	
    }

}




