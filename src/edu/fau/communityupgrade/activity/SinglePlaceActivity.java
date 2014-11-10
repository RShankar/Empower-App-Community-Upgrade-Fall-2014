package edu.fau.communityupgrade.activity;

import android.os.Bundle;
import android.util.Log;
import edu.fau.communityupgrade.R;
import edu.fau.communityupgrade.models.Place;

public class SinglePlaceActivity extends BaseActivity {

	public static final String PLACE_OBJECT_EXTRA_KEY = "SinglePLaceActivity.PlaceObjectExtra";
	private static final String TAG = "SinglePlaceActivity";
	
	private Place currentPlace;
	
	@Override
	public void onCreate(final Bundle savedInstance)
	{
		super.onCreate(savedInstance);
		currentPlace = (Place)getIntent().getParcelableExtra(PLACE_OBJECT_EXTRA_KEY);
		
		if(currentPlace == null)
		{
			Log.e(TAG,"Place is Null. Exiting Activity");
			this.finish();
		}
		
		setContentView(R.layout.place_single_layout);
		
	}
	
	
}
