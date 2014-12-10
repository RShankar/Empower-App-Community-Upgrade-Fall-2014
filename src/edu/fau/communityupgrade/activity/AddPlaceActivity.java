package edu.fau.communityupgrade.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import edu.fau.communityupgrade.R;
import edu.fau.communityupgrade.callback.DefaultSaveCallback;
import edu.fau.communityupgrade.database.PlaceManager;
import edu.fau.communityupgrade.models.Place;
import edu.fau.communityupgrade.models.PlaceBuilder;
import edu.fau.communityupgrade.ui.LoadingDialog;

public class AddPlaceActivity extends BaseActivity {

	private EditText placeNameText;
	private EditText placeDescriptionText;
	private EditText placeContactNumberText;
	private EditText placeAddressText;
	private EditText placeCityText;
	private EditText placeStateText;
	private EditText placeZipText;
	private CheckBox placeUseLocationCheckbox;
	
	private LoadingDialog loadingDialog;
	
	
	private PlaceManager placeManager;
	private Button addPlaceButton;
	@Override
	public void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);
		setContentView(R.layout.add_place_layout);
		
		placeManager = new PlaceManager(this);
		
		placeNameText = (EditText)findViewById(R.id.add_place_name);
		placeDescriptionText = (EditText)findViewById(R.id.add_place_description);
		placeContactNumberText = (EditText)findViewById(R.id.add_place_contact_number);
		placeAddressText = (EditText)findViewById(R.id.add_place_address);
		placeCityText = (EditText)findViewById(R.id.add_place_city);
		placeStateText = (EditText)findViewById(R.id.add_place_state);
		placeZipText = (EditText)findViewById(R.id.add_place_zip);
		placeUseLocationCheckbox= (CheckBox)findViewById(R.id.add_place_use_current_location);
		
		placeUseLocationCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked)
				{
					((View)placeAddressText.getParent()).setVisibility(View.GONE);
					
					((View)placeCityText.getParent()).setVisibility(View.GONE);
					
					((View)placeStateText.getParent()).setVisibility(View.GONE);
					
					((View)placeZipText.getParent()).setVisibility(View.GONE);
				}
				else
				{
					((View)placeAddressText.getParent()).setVisibility(View.VISIBLE);
					
					((View)placeCityText.getParent()).setVisibility(View.VISIBLE);
					
					((View)placeStateText.getParent()).setVisibility(View.VISIBLE);
					
					((View)placeZipText.getParent()).setVisibility(View.VISIBLE);
					
				}
				
			}
			
		});
		
		addPlaceButton = (Button)findViewById(R.id.add_place_btn);
		loadingDialog = new LoadingDialog(this,getString(R.string.dialog_saving_title),getString(R.string.dialog_saving_message));
		addPlaceButton.setOnClickListener(new AddPlaceButtonListener());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.add_place_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	
	private boolean isInputValid()
	{
		return  !placeNameText.getText().toString().isEmpty() &&
				!placeDescriptionText.getText().toString().isEmpty() &&
				!placeContactNumberText.getText().toString().isEmpty() &&
				!placeAddressText.getText().toString().isEmpty();
	}
	
	private void showAlertForInvalidInput()
	{
		Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.validate_data_title));
		alert.setMessage(getString(R.string.validate_data_message));
		alert.setPositiveButton(getString(R.string.default_confirmation),null);
		alert.show();
	}
	
	private class AddPlaceButtonListener implements OnClickListener
	{

		@Override
		public void onClick(View v) {

			if(!isInputValid())
			{
				showAlertForInvalidInput();
				return;
			}
			loadingDialog.show();
			
			final String address = placeAddressText.getText().toString();
			final String city = placeCityText.getText().toString();
			final String state = placeStateText.getText().toString();
			final String zip = placeZipText.getText().toString();
			
			final String name = placeNameText.getText().toString();
			final String description = placeDescriptionText.getText().toString();
			final String contactNumber = placeContactNumberText.getText().toString();
			
			Place place = new PlaceBuilder()
							.setAddress(address+" "+city+", "+state+" "+zip)
							.setName(name)
							.setDescription(description)
							.setContactNumber(contactNumber)
							.build();
			
			if(!placeUseLocationCheckbox.isChecked())
			{
				Log.d("AddPlaceActivity","Saving with address");
				placeManager.SavePlace(place, new SavePlaceCallback());	
			}
			else
			{
				placeManager.SavePlaceFromUserLocation(place, new SavePlaceCallback());
			}
			
		}
		
		private class SavePlaceCallback implements DefaultSaveCallback<Place>
		{

			@Override
			public void onSaveComplete(Place place) {
				loadingDialog.dismiss();
				Toast.makeText(AddPlaceActivity.this, getString(R.string.add_place_success), Toast.LENGTH_LONG).show();
				finish();
			}

			@Override
			public void onProviderNotAvailable() {
			}

			@Override
			public void onError(String error) {
				loadingDialog.dismiss();
				Toast.makeText(AddPlaceActivity.this, getString(R.string.add_place_error), Toast.LENGTH_LONG).show();
			}
			
		}
		
	}
}
