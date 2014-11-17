package edu.fau.communityupgrade.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import edu.fau.communityupgrade.callback.DefaultFindCallback;
import edu.fau.communityupgrade.callback.DefaultFindFirstCallback;
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
	
	public final static String VOTE_TABLE = "Vote";
	public final static String VOTE_IS_UPVOTE = "isUpvote";
	public final static String VOTE_COMMENT_ID = "commentId";
	public final static String VOTE_USER_ID = "userId";
	public final static String OBJECT_ID = "objectId";
	public final static String COMMENT_CONTENT = "comment_content";
	public final static String PLACE_ID = "placeId";
	public final static String CREATED_BY = "createdBy";
	public final static String PARENT_ID = "parentId";
	public final static String SCORE = "score";
	public final static String CREATED_AT = "createdAt";
	private final static String TAG = "CommentManager";
	
	//For current user
	private final HashMap<String,ParseObject> cacheOfParseVoteObjectsByCommentId;
	
	private final HashMap<String,ArrayList<Comment>> cacheOfCommentsByPlaceId;
	private final HashMap<String,ArrayList<Comment>> cacheOfCommentsByParentId;
	private final HashMap<String,ParseObject> cacheOfParseObjectsById;
	private ApplicationPreferenceManager preferenceManager;
	private UserManager userManager;
	private final Context mContext;
	
	public CommentManager(final Context context)
	{
		mContext = context;
		preferenceManager = new ApplicationPreferenceManager(context);
		preferenceManager.clearrAllCommentsSavedTimes();
		userManager = UserManager.getInstance();
		cacheOfCommentsByParentId = new HashMap<String,ArrayList<Comment>>();
		cacheOfCommentsByPlaceId = new HashMap<String, ArrayList<Comment>>();
		cacheOfParseObjectsById = new HashMap<String,ParseObject>();
		cacheOfParseVoteObjectsByCommentId = new HashMap<String,ParseObject>();
	}
	
	
	
	protected void getCommentById(final String objectId, final DefaultFindFirstCallback<ParseObject> callback)
	{
		if(useCacheForParseObject(objectId))
		{
			callback.onComplete(this.getCacheForParseObject(objectId));
			return;
		}
		ParseQuery<ParseObject> query = ParseQuery.getQuery(TABLE);
		query.whereEqualTo(OBJECT_ID, objectId);
		setVotesForUser();
		query.getFirstInBackground(new GetCallback<ParseObject>(){

			@Override
			public void done(ParseObject c, ParseException e) {
			
				//Todo:Update Cache with ParseObject
				if(e != null)
				{
					callback.onError(e.toString());
					return;
				}
				if(c  == null)
				{
					callback.onError("ID Does not exist.");
					return;
				}
				callback.onComplete(c);
			}
		});
		
	}
	
	/**
	 * Can only be used by Other managers. Used to get Comments for each Place.
	 * Assumed:
	 * 	Already ran in separate Thread.
	 * @param parseObject
	 * @return
	 */
	public ArrayList<Comment> getCommentsByPlace(ParseObject parseObject)
	{
		setVotesForUser();
		if(this.useCacheForPlace(parseObject))
		{
			return this.getCacheForPlace(parseObject);
		}
		ParseQuery<ParseObject> commentQuery = ParseQuery.getQuery(TABLE);
		commentQuery.include(CREATED_BY);
		commentQuery.whereEqualTo(PLACE_ID, parseObject);
		commentQuery.whereDoesNotExist(PARENT_ID);
		commentQuery.addDescendingOrder(SCORE);
		commentQuery.addAscendingOrder(CREATED_AT);
		List<ParseObject> parseObjects;
		
		
		
		try {
			parseObjects = commentQuery.find();
		} 
		catch (ParseException e) {
			e.printStackTrace();
			return null;
		}

		ParseObject.pinAllInBackground(parseObject.getObjectId(),parseObjects);
		ArrayList<Comment> list = new ArrayList<Comment>();
		
		
		for(int i=0;i<parseObjects.size();i++)
		{
			ParseQuery<ParseObject> voteQuery = new ParseQuery<ParseObject>(VOTE_TABLE);
			
			voteQuery.whereEqualTo(VOTE_USER_ID, UserManager.getInstance().getParseUser());
			voteQuery.whereEqualTo(VOTE_COMMENT_ID, parseObjects.get(i));
			ParseObject vote;
			
			try{
				vote = voteQuery.getFirst();
			}
			catch(ParseException e)
			{
				Log.e(TAG,"Error retrieving vote: ",e);
				vote = null;
			}
			
			list.add(ParseHelper.parseObjectToComment(parseObjects.get(i),vote));
		}
		
		return list;
	}
	
	/**
	 * Returns all comments to callback or an empty list if empty
	 * @param comment
	 * @param callback
	 */
	public void getChildComments(final Comment comment, final DefaultFindCallback<Comment> callback)
	{
		setVotesForUser();
		if(this.useCacheForParent(comment))
		{
			callback.onComplete(getCacheForParent(comment));
			return;
		}
		
		ParseQuery<ParseObject> parentQuery = new ParseQuery<ParseObject>(TABLE);
		Log.d(TAG,"getChildComments: "+comment.toString());
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
	
	
	public void addVote(final String commentId, final boolean isUpvote)
	{
		final ParseUser user = UserManager.getInstance().getParseUser();
		
		
		this.getCommentById(commentId, new DefaultFindFirstCallback<ParseObject>(){
			@Override
			public void onComplete(final ParseObject commentObject) {
				
				if(cacheOfParseVoteObjectsByCommentId.containsKey(commentId))
				{ 
					ParseObject vote = cacheOfParseVoteObjectsByCommentId.get(commentId);
					vote.put(VOTE_IS_UPVOTE, isUpvote);
					vote.saveEventually();
					return;
				}
				else {
					ParseObject saveObject = new ParseObject(VOTE_TABLE);
					saveObject.put(VOTE_USER_ID, user);
					saveObject.put(VOTE_COMMENT_ID, commentObject);
					saveObject.put(VOTE_IS_UPVOTE, isUpvote);
					saveObject.saveEventually();
					cacheOfParseVoteObjectsByCommentId.put(commentObject.getObjectId(), saveObject);
				}
					
				int score = commentObject.getInt(SCORE);
				if(isUpvote)
				{
					score++;
				}
				else
				{  
					score--;
				}
				
				commentObject.put(SCORE, score);
				commentObject.saveEventually();
				updateCache(commentObject);
				
			}

			@Override
			public void onProviderNotAvailable() {
			}

			@Override
			public void onError(String error) {
			}
		});
		
	}
	
	public void saveComment(final Comment comment, final DefaultSaveCallback<Comment> Callback)
	{
		PlaceManager placeManager = new PlaceManager(mContext);
		final ParseObject saveObject = new ParseObject(TABLE);
		Log.d(TAG,"Comment Saving!");
		saveObject.put(COMMENT_CONTENT, comment.getComment_content());
		saveObject.put(CREATED_BY, userManager.getParseUser());
		saveObject.put(SCORE, 0);
		
		placeManager.getParseObjectPlaceById(comment.getPlaceId(), new DefaultFindFirstCallback<ParseObject>(){
			@Override
			public void onComplete(ParseObject object) {
				saveObject.put(PLACE_ID,object);
				if(comment.getParentId() != null){
					getCommentById(comment.getParentId(), new DefaultFindFirstCallback<ParseObject>(){

						@Override
						public void onComplete(ParseObject object) {
							saveObject.put(PARENT_ID, object);
							saveObject.saveEventually(new CommentSaveCallback(Callback, saveObject));
						}

						@Override
						public void onProviderNotAvailable() {
						}

						@Override
						public void onError(String error) {
							Callback.onError(error);
							Log.e(TAG,error);
						}
					});
				}
				else
				{
					saveObject.saveEventually(new CommentSaveCallback(Callback, saveObject));
				}
			}

			@Override
			public void onProviderNotAvailable() {
			}

			@Override
			public void onError(String error) {
				Callback.onError(error.toString());
				Log.e(TAG,error);
			}
		});
	}
	
	private boolean useCacheForPlace(ParseObject parseObject)
	{
		if(parseObject == null || parseObject.getObjectId() == null)
			return false;
		return cacheOfCommentsByPlaceId.containsKey(parseObject.getObjectId());
	
	}
	
	private ArrayList<Comment> getCacheForPlace(ParseObject parseObject)
	{
		if(parseObject == null || parseObject.getObjectId() == null)
			return null;
		return cacheOfCommentsByPlaceId.get(parseObject.getObjectId());
	}
	
	private boolean useCacheForParseObject(String id)
	{
		if(id == null || id == "")
		{
			return false;
		}
		
		return cacheOfParseObjectsById.containsKey(id);
	}
	
	private ParseObject getCacheForParseObject(String id)
	{
		if(id == null || id == "")
		{
			return null;
		}
		
		return cacheOfParseObjectsById.get(id);
	}
	
	private boolean useCacheForParent(Comment comment)
	{
		if(comment.getObjectId() == null || comment.getObjectId().isEmpty()){
			return false;
		}
		return cacheOfCommentsByParentId.containsKey(comment.getObjectId());
	}
	
	private ArrayList<Comment> getCacheForParent(Comment comment)
	{
		if(comment.getObjectId() == null || comment.getObjectId().isEmpty()){
			return null;
		}
		return cacheOfCommentsByParentId.get(comment.getObjectId());
	}
	
	private void setVotesForUser()
	{
		if(!cacheOfParseVoteObjectsByCommentId.isEmpty())
			return;
		
		ParseUser currentUser = userManager.getParseUser();
		
		ParseQuery<ParseObject> votesQuery = new ParseQuery<ParseObject>(VOTE_TABLE);
		votesQuery.whereEqualTo(VOTE_USER_ID, currentUser);
		votesQuery.include(VOTE_COMMENT_ID);
		votesQuery.findInBackground(new FindCallback<ParseObject>(){

			@Override
			public void done(List<ParseObject> votes, ParseException e) {
				if(e != null)
				{
					Log.e(TAG,"Error getting Votes: ",e);
					return;
				}
				for(int i=0;i<votes.size();i++)
				{
					ParseObject currentVote =  votes.get(i);
					//Update Cache with Votes
					cacheOfParseVoteObjectsByCommentId.put(currentVote.getParseObject(VOTE_COMMENT_ID).getObjectId(), currentVote);
				}
				
			}
			
			
		});
	}
	
	/**
		Updates Cache for a collection of comments.
	*/
	private void updateCache(List<ParseObject> comments)
	{
		for(int i=0;i<comments.size();i++)
		{
			updateCache(comments.get(i));
		}
	}
	
	/**

	*/
	private void updateCache(ParseObject object)
	{
		if(object == null || object.getObjectId() == null)
			return;
		Comment comment = ParseHelper.parseObjectToComment(object);
		
		this.cacheOfParseObjectsById.put(object.getObjectId(), object);
		if(comment.getParentId() != null)
		{
			if(!cacheOfCommentsByParentId.containsKey(comment.getParentId()))
			{
				cacheOfCommentsByParentId.put(comment.getParentId(), new ArrayList<Comment>());
			}
			if(!cacheOfCommentsByParentId.get(comment.getParentId()).contains(comment))
				cacheOfCommentsByParentId.get(comment.getParentId()).add(comment);
		}
		else if(comment.getPlaceId() != null && !comment.getPlaceId().isEmpty())
		{
			if(!cacheOfCommentsByPlaceId.containsKey(comment.getPlaceId()))
			{
				cacheOfCommentsByPlaceId.put(comment.getPlaceId(), new ArrayList<Comment>());
			}
			if(!cacheOfCommentsByPlaceId.get(comment.getPlaceId()).contains(comment))
				cacheOfCommentsByPlaceId.get(comment.getPlaceId()).add(comment);
		
		}
	}
	
	
	private class CommentSaveCallback extends SaveCallback
	{
		private final DefaultSaveCallback<Comment> callback;
		
		private final ParseObject parseComment;
		
		private Comment comment;
		
		public CommentSaveCallback(final DefaultSaveCallback<Comment> c, final ParseObject pcomment)
		{
			callback = c;
			parseComment = pcomment;
		}

		@Override
		public void done(ParseException e) {
		
			
			updateCache(parseComment);
			this.comment = ParseHelper.parseObjectToComment(parseComment);
			List<ParseObject> list = new ArrayList<ParseObject>();
			list.add(parseComment);
			ParseObject.pinAllInBackground(list);
			if(e != null)
			{
				callback.onError(e.toString());
				return;
			}
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
			
			if(e != null)
			{
				callback.onError(e.toString());
				return;
			}
			
			updateCache(list);
			ParseObject.pinAllInBackground(list);
			ArrayList<Comment> comments = new ArrayList<Comment>();
			for(int i=0;i<list.size();i++)
			{
				ParseObject VoteObject = cacheOfParseVoteObjectsByCommentId.get(list.get(i).getObjectId());
				Comment comment = ParseHelper.parseObjectToComment(list.get(i),VoteObject);
				comments.add(comment);
				
			}
			
			callback.onComplete(comments);
		}
		
	}
}
