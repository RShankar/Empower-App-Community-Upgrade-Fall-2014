package edu.fau.communityupgrade.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import edu.fau.communityupgrade.callback.DefaultFindCallback;
import edu.fau.communityupgrade.callback.DefaultSaveCallback;
import edu.fau.communityupgrade.helper.ParseHelper;
import edu.fau.communityupgrade.models.Comment;
import edu.fau.communityupgrade.preferences.ApplicationPreferenceManager;
/**
 * This class connects to the Parse Database
 * and handles all DB connections related to Comments.
 * 
 * @author kyle
 *
 */
public class CommentManager {
	
	public static String TABLE = "Comment";
	
	public final static String OBJECT_ID = "objectId";
	public final static String COMMENT_CONTENT = "comment_content";
	public final static String PLACE_ID = "placeId";
	public final static String CREATED_BY = "createdBy";
	public final static String PARENT_ID = "parentId";
	public final static String SCORE = "score";
	
	private final static String TAG = "CommentManager";
	private final HashMap<String,ArrayList<Comment>> cacheOfComments;
	private ApplicationPreferenceManager preferenceManager;
	private UserManager userManager;
	
	public CommentManager(Context context)
	{
		preferenceManager = new ApplicationPreferenceManager(context);
		preferenceManager.clearrAllCommentsSavedTimes();
		cacheOfComments = new HashMap<String,ArrayList<Comment>>();
		userManager = UserManager.getInstance();
	}
	
	/**
	 * Can only be used by Other managers.
	 * Assumed:
	 * 	Already ran in separate Thread.
	 * @param parseObject
	 * @return
	 */
	protected ArrayList<Comment> getCommentsByPlace(ParseObject parseObject)
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE);
		
		long lastSaved = preferenceManager.getLastCommentSavedTime(parseObject.getObjectId());
		
		if(System.currentTimeMillis() - lastSaved < PlaceManager.CACHE_EXPIRE_TIME_MILLI
				&& cacheOfComments.containsKey(parseObject.getObjectId()))
		{
			return cacheOfComments.get(parseObject.getObjectId());
		}
		
		query.include(CREATED_BY);
		query.whereEqualTo(PLACE_ID, parseObject);
		query.whereDoesNotExist(PARENT_ID);
		query.orderByDescending(SCORE);
		List<ParseObject> parseObjects;
		
		try {
			parseObjects = query.find();
		} 
		catch (ParseException e) {
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
		
		updateCache(parseObject.getObjectId(),list);
		
		return list;
	}
	
	/**
	 * Replaces current Cache with new collection for Place with Id objectId
	 * @param objectId
	 * @param comments
	 */
	private void updateCache(final String objectId,Collection<Comment> comments )
	{
		if(!cacheOfComments.containsKey(objectId))
		{
			cacheOfComments.put(objectId, new ArrayList<Comment>());
		}
		cacheOfComments.get(objectId).clear();
		cacheOfComments.get(objectId).addAll(comments);
	}
	
	
	/**
	 * Returns all comments to callback or an empty list if empty
	 * @param comment
	 * @param callback
	 */
	public void getChildComments(final Comment comment, final DefaultFindCallback<Comment> callback)
	{
		ParseQuery<ParseObject> parentQuery = new ParseQuery<ParseObject>(TABLE);
		parentQuery.whereEqualTo(OBJECT_ID, comment.getObjectId());
		parentQuery.fromLocalDatastore();
		parentQuery.getFirstInBackground(new GetCallback<ParseObject>(){
			@Override
			public void done(ParseObject parent, ParseException e) {
				if(e != null)
				{
					callback.onError(e.toString());
				}
				final ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(TABLE);
				query.orderByDescending(SCORE);
				query.include(CREATED_BY);
				query.whereEqualTo(PARENT_ID, parent);
				query.findInBackground(new CommentFindCallback(callback));	
			}
		});
		
	}
	
	public void saveComment(final Comment comment, final DefaultSaveCallback<Comment> Callback)
	{
		ParseObject saveObject = new ParseObject(TABLE);
		
		saveObject.put(COMMENT_CONTENT, comment.getComment_content());
		saveObject.put(CREATED_BY, userManager.getParseUser());
		saveObject.put(PLACE_ID, comment.getPlaceId());
		saveObject.put(PARENT_ID, comment.getParentId());
		saveObject.put(SCORE, 0);
		saveObject.saveEventually(new CommentSaveCallback(Callback, saveObject));
	}
	
	private class CommentSaveCallback extends SaveCallback
	{
		private final DefaultSaveCallback<Comment> callback;
		
		private final Comment comment;
		
		public CommentSaveCallback(final DefaultSaveCallback<Comment> c, final ParseObject comment)
		{
			callback = c;
			
			this.comment = ParseHelper.parseObjectToComment(comment);
		}

		@Override
		public void done(ParseException e) {
			if(e != null)
			{
				callback.onError(e.toString());
				return;
			}
			ArrayList<Comment> placesComments = cacheOfComments.get(comment.getPlaceId());
			if(placesComments == null)
			{
				placesComments = new ArrayList<Comment>();
			}
			

			placesComments.add(comment);
			cacheOfComments.put(comment.getPlaceId(), placesComments);
			
			callback.onSaveComplete(comment);
			
			
		}
		
	}
	
	private class CommentFindCallback extends FindCallback<ParseObject>
	{
		private final DefaultFindCallback<Comment> callback;
		
		public CommentFindCallback(final DefaultFindCallback<Comment> c)
		{
			callback = c;
		}
		
		@Override
		public void done(List<ParseObject> list, ParseException e) {
			Log.d(TAG,list.toString());
			if(e != null)
			{
				callback.onError(e.toString());
				return;
			}
			
			ParseObject.pinAllInBackground(list);
			
			ArrayList<Comment> comments = new ArrayList<Comment>();
			for(int i=0;i<list.size();i++)
			{
				comments.add(ParseHelper.parseObjectToComment(list.get(i)));
			}
			
			callback.onComplete(comments);
		}
		
	}
}
