package edu.fau.communityupgrade.database;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import edu.fau.communityupgrade.callback.DefaultParseCallback;
import edu.fau.communityupgrade.helper.ParseHelper;
import edu.fau.communityupgrade.models.Place;

/**
 * This class connects to the Parse Database
 * and handles all DB connections related to Places.
 * @author kyle
 *
 */
public class PlaceManager {

	//Name of the DB columns
	public static String OBJECT_ID = "objectId";
	public static String CREATED_BY = "createdBy";
	public static String CREATED_AT = "createdAt";
	public static String NAME = "name";
	public static String CONTACT_NAME = "contactName";
	public static String CONTACT_NUMBER = "contactNumber";
	public static String GEOPOINT = "location";
	
	public static String TABLE = "Place";
	public static String TAG = "PlaceManager";
	private CommentManager commentManager;
	
	public PlaceManager()
	{
		commentManager = new CommentManager();
	}

	/**
	 * Returns an ArrayList of all Places initialized by current
	 * user. Ran in a background thread.
	 * @return
	 */
	public void getAllPlacesCreatedByCurrentUser(DefaultParseCallback callback)
	{
		UserManager userManager  = UserManager.getInstance();
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE);
		query.include(CREATED_BY);
		query.whereEqualTo(CREATED_BY, userManager.getParseUser());
		
		query.findInBackground(new PlaceFindCallback(callback));
	}
	
	/**
	 * This Callback is used to handle the information once the find has 
	 * happened from the previous thread. Requires passing a instance
	 * of DefaultParseCallback to send the information back to the Activity.
	 * @author kyle
	 *
	 */
	private class PlaceFindCallback extends FindCallback<ParseObject>
	{
		DefaultParseCallback callback;
		
		public PlaceFindCallback(DefaultParseCallback callback)
		{
			this.callback = callback;
		}
		
		@Override
		public void done(List<ParseObject> list, ParseException e) {
			
			if(e != null)
			{
				Log.e(TAG,"Error retrieving places: ",e);
				callback.onError();
				return;
			}
			
			CommentManager cManager = new CommentManager();
			
			final ArrayList<Place> placesList = new ArrayList<Place>();
			for(int i=0;i<list.size();i++)
			{
				Place place = ParseHelper.parseObjectToPlace(list.get(i));
				place.setComments(cManager.getCommentsByPlace(list.get(i)));
				Log.d(TAG,"TEST: "+place);
				placesList.add(place);
			}
			
			callback.onComplete(placesList);
		}
		
	}
	
	
}
