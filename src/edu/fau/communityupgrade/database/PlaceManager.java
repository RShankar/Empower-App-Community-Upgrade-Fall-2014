package edu.fau.communityupgrade.database;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
	public PlaceManager()
	{
		
	}

	/**
	 * Returns an ArrayList of all Places initialized by current
	 * user.
	 * @return
	 */
	public ArrayList<Place> getAllPlacesCreatedByCurrentUser()
	{
		UserManager userManager = UserManager.getInstance();
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE);
		
		final ArrayList<Place> placesList = new ArrayList<Place>();
		
		
		List<ParseObject> list = new ArrayList<ParseObject>();
		try 
		{
			list.addAll(query.find());
		} 
		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i=0;i<list.size();i++)
		{
			 
			Place place = ParseHelper.parseObjectToPlace(list.get(i));
			Log.d(TAG,"TEST: "+place);
			placesList.add(place);
		}
		
		return placesList;
	}
	
	
}
