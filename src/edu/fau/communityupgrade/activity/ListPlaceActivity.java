package edu.fau.communityupgrade.activity;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import edu.fau.communityupgrade.R;
import edu.fau.communityupgrade.callback.DefaultFindCallback;
import edu.fau.communityupgrade.database.PlaceManager;
import edu.fau.communityupgrade.maps.mapActivity;
import edu.fau.communityupgrade.models.Place;
import edu.fau.communityupgrade.ui.LoadingDialog;

public class ListPlaceActivity extends BaseActivity {

	private static final String TAG = "ListPlaceActivity";
	private int COLOR_SELECTED_PLACE;
	
	private View selectedView;
	private PlaceManager placeManager;
	private LoadingDialog loadingDialog;
	
	private ListView placeListView;
	private PlaceAdapter placeAdapter;
	private int RADIUS;
	private SharedPreferences preferences;
	
	@Override
	public void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);
		preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		setContentView(R.layout.place_list_layout);
		RADIUS = preferences.getInt(RADIUS_IN_MILES, RADIUS_IN_MILES_DEFAULT);
		COLOR_SELECTED_PLACE = getResources().getColor(R.color.selected_item);
		
		getActionBar().setTitle(preferences.getString(LIST_PAGE_TITLE, LIST_PAGE_TITLE_DEFAULT));
		
		placeManager = new PlaceManager(this);
		loadingDialog = new LoadingDialog(this);
		placeAdapter = new PlaceAdapter();
		selectedView = null;
		
		placeListView = (ListView)findViewById(R.id.place_list_view);
		placeListView.setAdapter(placeAdapter);
		placeListView.setOnItemClickListener(new PlaceClickListener());
		

	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		updateList();
	}
	
	
	private void updateList()
	{
		loadingDialog.show();
		placeManager.getAllPlacesNearUser((double)RADIUS, new PlaceFindCallback());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.list_activity_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_map:
	        	Intent intent = new Intent(this,mapActivity.class);
	        	startActivity(intent);
	            return true;
	        case R.id.action_settings:
	            return true;
	        case R.id.action_refresh:
	        	placeManager.clearCache();
	        	updateList();
	        	return true;
	        case R.id.action_logout:
	        	logout();
	            return true;
	        case R.id.action_add_place:
	        	Intent addPlaceIntent = new Intent(this,AddPlaceActivity.class);
	        	startActivity(addPlaceIntent);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void goToSinglePlaceActivity(final Place place)
	{
		Intent intent = new Intent(ListPlaceActivity.this,SinglePlaceActivity.class);
		
		Bundle extras = new Bundle();
		extras.putParcelable(SinglePlaceActivity.PLACE_OBJECT_EXTRA_KEY, place);
		intent.putExtras(extras);
		startActivity(intent);
	}
	
	private class PlaceClickListener implements OnItemClickListener
	{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			if(selectedView != null)
			{
				selectedView.setBackgroundColor(Color.TRANSPARENT);
				final ImageView selectedGoToCommentsBtn = (ImageView)selectedView.findViewById(R.id.go_to_comments);
				selectedGoToCommentsBtn.setVisibility(View.GONE);
			}
			final ImageView gotoCommentsBtn = (ImageView)view.findViewById(R.id.go_to_comments);
			gotoCommentsBtn.setVisibility(View.VISIBLE);
			
			view.setBackgroundColor(COLOR_SELECTED_PLACE);
			
			selectedView = view;
		}
	}
	
	private class PlaceAdapter extends ArrayAdapter<Place>
	{

		public PlaceAdapter() {
			super(ListPlaceActivity.this, 0);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			// Get the data item for this position
			final Place place = getItem(position);    
	       // Check if an existing view is being reused, otherwise inflate the view
	       if (convertView == null) {
	          convertView = LayoutInflater.from(getContext()).inflate(R.layout.place_list_item_view, parent, false);
	       }
	       
	       final TextView placeNameTextView = (TextView)convertView.findViewById(R.id.place_name);
	       final TextView placeDescriptionTextView = (TextView)convertView.findViewById(R.id.place_description);
	       final TextView placeNumberOfCommentsTextView = (TextView)convertView.findViewById(R.id.place_number_comments);
	       final ImageView goToCommentsView = (ImageView)convertView.findViewById(R.id.go_to_comments);
	       
	       
	       final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	       placeNameTextView.setText(place.getName());
	       
	       //Update UI From Processing
	       placeNameTextView.setTextColor(Color.parseColor(preferences.getString(MAIN_COLOR, MAIN_COLOR_DEFAULT)));
	       placeNameTextView.setTextSize(preferences.getInt(TITLE_TEXT_SIZE,TITLE_TEXT_SIZE_DEFAULT));
	       
	       placeDescriptionTextView.setText(place.getDescription());
	       placeDescriptionTextView.setTextSize(preferences.getInt(DEFAULT_TEXT_SIZE,DEFAULT_TEXT_SIZE_DEFAULT));
	       
	       placeNumberOfCommentsTextView.setText(place.getComments().size()+" comments");
	       
	       goToCommentsView.setOnClickListener(new OnClickListener()
	       {
			@Override
			public void onClick(View v) {
				goToSinglePlaceActivity(place);
				
			}
	       });
	       
	       return convertView;
		}
		
	}
	
	/**
	 * This callback is used after the places have been found. 
	 * Use this to retrieve the Places and place them in the view.
	 * @author kyle
	 *
	 */
	private class PlaceFindCallback implements DefaultFindCallback<Place>{
		@Override
		public void onComplete(ArrayList<Place> list) {
			
			Log.d(TAG,"Downloaded Places: "+list.toString());
			
			
			placeAdapter.clear();
			placeAdapter.addAll(list);
			loadingDialog.dismiss();
			
		}

		@Override
		public void onError(String error) {
			Log.d(TAG,error);
			loadingDialog.dismiss();
			
		}

		@Override
		public void onProviderNotAvailable() {
			loadingDialog.dismiss();
			Builder alert = new AlertDialog.Builder(ListPlaceActivity.this);
			alert.setTitle(getString(R.string.error_no_provider_title));
			alert.setMessage(getString(R.string.error_no_provider_message));
			alert.setPositiveButton(getString(R.string.go_to_location_settings),new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(intent);
				}

				
			});
			alert.setNegativeButton(getString(R.string.close_app), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
					
				}
			});
			
			alert.show(); 
			
		}
	}
}
