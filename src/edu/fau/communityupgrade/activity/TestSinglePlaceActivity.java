package edu.fau.communityupgrade.activity;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.fau.communityupgrade.R;
import edu.fau.communityupgrade.models.Comment;
import edu.fau.communityupgrade.models.Place;

public class TestSinglePlaceActivity extends BaseActivity {

	//Key to retrieve Place from
	public static final String PLACE_OBJECT_EXTRA = "edu.fau.communityupgrade.activity.TestSinglePlaceActivity.PlaceObjectExtra";
	
	private Place place;
	private ListView commentListView;
	ArrayList<Comment> comments;
	CommentsAdapter adapter;
	@Override
	public void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);
		setContentView(R.layout.testplace_single_layout);
		place = (Place)getIntent().getParcelableExtra(PLACE_OBJECT_EXTRA);
		commentListView = (ListView)findViewById(R.id.test_comments_list_view);
		
		comments = new ArrayList<Comment>();
		
		//adapter = new CommentsAdapter(this,comments);
		
		commentListView.setAdapter(adapter);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
	}
	
	
	/**
	 * This Adapter is used to list each comment on the list view in the layout
	 * @author kyle
	 *
	 */
	private class CommentsAdapter extends ArrayAdapter<Comment>
	{

		public CommentsAdapter(Context context, ArrayList<Comment> objects) {
			super(context, 0, objects);
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
