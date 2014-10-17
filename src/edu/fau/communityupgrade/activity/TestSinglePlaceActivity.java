package edu.fau.communityupgrade.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import edu.fau.communityupgrade.R;
import edu.fau.communityupgrade.models.Comment;
import edu.fau.communityupgrade.models.Place;

public class TestSinglePlaceActivity extends BaseActivity {

	//Key to retrieve Place from
	public static final String PLACE_OBJECT_EXTRA = "edu.fau.communityupgrade.activity.TestSinglePlaceActivity.PlaceObjectExtra";
	
	private Place place;
	
	@Override
	public void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);
		setContentView(R.layout.testplace_single_layout);
		place = (Place)getIntent().getParcelableExtra(PLACE_OBJECT_EXTRA);
	}
	
	private class CommentsAdapter extends ArrayAdapter<Comment>
	{

		public CommentsAdapter(Context context, int resource, Comment[] objects) {
			super(context, resource, objects);
		}
		
		@Override
		public View getView(int position,View convertView, ViewGroup parent)
		{
			//Get data
			Comment comment = getItem(position);
			
			// Check if an existing view is being reused, otherwise inflate the view
		    if (convertView == null) {
		    	convertView = LayoutInflater.from(getContext()).inflate(R.layout.testplace_single_comment_item, parent, false);
		    }
		    
		    return null;
		}

		
		
		
	}
}
