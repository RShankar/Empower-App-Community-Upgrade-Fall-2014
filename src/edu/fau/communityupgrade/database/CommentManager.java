package edu.fau.communityupgrade.database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import edu.fau.communityupgrade.helper.ParseHelper;
import edu.fau.communityupgrade.models.Comment;
import edu.fau.communityupgrade.preferences.ApplicationPreferenceManager;

public class CommentManager {
	
	public static String TABLE = "Comment";
	
	public final static String OBJECT_ID = "objectId";
	public final static String COMMENT_CONTENT = "comment_content";
	public final static String PLACE_ID = "placeId";
	public final static String CREATED_BY = "createdBy";
	public final static String PARENT_ID = "parentId";
	public final static String SCORE = "score";
	
	private final static String TAG = "CommentManager";
	
	
	private ApplicationPreferenceManager preferenceManager;
	
	public CommentManager(Context context)
	{
		preferenceManager = new ApplicationPreferenceManager(context);
		
	}
	
	/**
	 * Can only be used by Other managers.
	 * Assumed:
	 * 	Already be ran in separate Thread.
	 * @param parseObject
	 * @return
	 */
	protected ArrayList<Comment> getCommentsByPlace(ParseObject parseObject)
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE);
		
		long lastSaved = preferenceManager.getLastCommentSavedTime(parseObject.getObjectId());
		if(System.currentTimeMillis() - lastSaved < PlaceManager.CACHE_EXPIRE_TIME_MILLI)
		{
			Log.d(TAG,"Attempting to use Cache for Place: "+parseObject.getString(PlaceManager.NAME));
			query.fromPin(parseObject.getObjectId());
		}
		
		query.include(CREATED_BY);
		query.whereEqualTo(PLACE_ID, parseObject);
		query.whereEqualTo(PARENT_ID, null);
		query.orderByDescending(SCORE);
		List<ParseObject> parseObjects;
		
		try {
			parseObjects = query.find();
		} 
		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		ParseObject.pinAllInBackground(parseObject.getObjectId(),parseObjects);
		//Log.d(TAG,parseObjects.toString());
		
		ArrayList<Comment> list = new ArrayList<Comment>();
		
		
		for(int i=0;i<parseObjects.size();i++)
		{
			list.add(ParseHelper.parseObjectToComment(parseObjects.get(i)));
		}
		
		return list;
	}
	
	public void saveComment(Comment comment)
	{
		
		
		
	}
}
