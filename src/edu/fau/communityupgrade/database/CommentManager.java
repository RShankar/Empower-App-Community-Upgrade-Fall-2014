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
import edu.fau.communityupgrade.models.Vote;
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
	public final static String VOTE_OBJECT_ID = "objectId";
	public final static String OBJECT_ID = "objectId";
	public final static String COMMENT_CONTENT = "comment_content";
	public final static String PLACE_ID = "placeId";
	public final static String CREATED_BY = "createdBy";
	public final static String PARENT_ID = "parentId";
	public final static String SCORE = "score";
	public final static String CREATED_AT = "createdAt";
	private final static String TAG = "CommentManager";
	
	//For current user
	/*private final HashMap<String,ParseObject> cacheOfParseVoteObjectsByCommentId;
	
	private final HashMap<String,ArrayList<Comment>> cacheOfCommentsByPlaceId;
	private final HashMap<String,ArrayList<Comment>> cacheOfCommentsByParentId;
	private final HashMap<String,ParseObject> cacheOfParseObjectsById;*/
	private CacheManager cacheManager;
	private ApplicationPreferenceManager preferenceManager;
	private UserManager userManager;
	private final Context mContext;
	
	public CommentManager(final Context context)
	{
		mContext = context;
		preferenceManager = new ApplicationPreferenceManager(context);
		preferenceManager.clearrAllCommentsSavedTimes();
		userManager = UserManager.getInstance();
		cacheManager = CacheManager.getInstance();
		/*cacheOfCommentsByParentId = new HashMap<String,ArrayList<Comment>>();
		cacheOfCommentsByPlaceId = new HashMap<String, ArrayList<Comment>>();
		cacheOfParseObjectsById = new HashMap<String,ParseObject>();
		cacheOfParseVoteObjectsByCommentId = new HashMap<String,ParseObject>();
		*/
	}
	
	public void clearCache()
	{
		/*cacheOfCommentsByParentId.clear();
		cacheOfCommentsByPlaceId.clear();
		cacheOfParseObjectsById.clear();
		cacheOfParseVoteObjectsByCommentId.clear();*/
	}
	
	protected void getCommentById(final String objectId, final DefaultFindFirstCallback<ParseObject> callback)
	{
		if(cacheManager.containsParseObjectId(objectId))
		{
			Log.d(TAG,"returning cached object");
			callback.onComplete(cacheManager.getParseObjectById(objectId));
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
				Log.d(TAG,"returning queried object");
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
		String placeId = parseObject.getObjectId();
		List<ParseObject> parseObjects;
		
		if(cacheManager.hasCommentsForPlaceId(placeId))
		{
			Log.d(TAG,"getCommentsByPlaceId: Using Cache.");
			parseObjects = cacheManager.getCommentsByPlaceId(parseObject.getObjectId()); 
		}
		else
		{
			ParseQuery<ParseObject> commentQuery = ParseQuery.getQuery(TABLE);
			commentQuery.include(CREATED_BY);
			commentQuery.include(PLACE_ID);
			commentQuery.include(CREATED_AT);
			commentQuery.whereEqualTo(PLACE_ID, parseObject);
			commentQuery.whereDoesNotExist(PARENT_ID);
			commentQuery.addDescendingOrder(SCORE);
			commentQuery.addAscendingOrder(CREATED_AT);
			
			try {
				parseObjects = commentQuery.find();
				if(parseObjects.isEmpty())
				{
					Log.d(TAG,"adding empty cache for place id: "+placeId);
					cacheManager.addEmptyCommentsForPlaceId(placeId);
				}
				cacheManager.addCommentParseObject(parseObjects);
			} 
			catch (ParseException e) {
				Log.e(TAG,"Error getting comment objects:",e);
				e.printStackTrace();
				return null;
			}
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
				//Log.e(TAG,"Error retrieving vote: ",e);
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
		
		final CommentFindCallback commentCallback = new CommentFindCallback(callback);
		
		if(cacheManager.hasCommentChildObjects(comment.getObjectId()))
		{
			commentCallback.done(cacheManager.getCommentChildObjects(comment.getObjectId()), null);
			return;
		}
		
		
		ParseQuery<ParseObject> parentQuery = new ParseQuery<ParseObject>(TABLE);
		//Log.d(TAG,"getChildComments: "+comment.toString());
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
				query.include(CREATED_AT);
				query.include(PLACE_ID);
				query.whereEqualTo(PARENT_ID, parent);
				query.findInBackground(commentCallback);	
			}
		});
	}
	
	
	public void addVote(final String commentId, final boolean isUpvote)
	{
		//Log.d(TAG,"addVote: ");
		final ParseUser user = UserManager.getInstance().getParseUser();
		
		this.getCommentById(commentId, new DefaultFindFirstCallback<ParseObject>(){
			@Override
			public void onComplete(final ParseObject commentObject) {
				
				//Vote already exists, updating value and incre/decre menting by 2s
				if(cacheManager.hasVoteByCommentId(commentId))
				{ 
					ParseObject vote = cacheManager.getVoteByCommentId(commentId);
					double score = commentObject.getDouble(SCORE);
					if(isUpvote)
					{
						score += 2;
					}
					else
					{
						score -= 2;
					}
					commentObject.put(SCORE, score);
					
					vote.put(VOTE_IS_UPVOTE, isUpvote);
					commentObject.saveEventually();
					vote.saveEventually();
					return;
				}
				else {
					final ParseObject saveObject = new ParseObject(VOTE_TABLE);
					saveObject.put(VOTE_USER_ID, user);
					saveObject.put(VOTE_COMMENT_ID, commentObject);
					saveObject.put(VOTE_IS_UPVOTE, isUpvote);
					saveObject.saveEventually(new SaveCallback(){

						@Override
						public void done(ParseException arg0) {
							cacheManager.addVoteParseObject(saveObject);
						}
						
						
					});
					
					double score = commentObject.getDouble(SCORE);
					
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
				}
				
				
			}

			@Override
			public void onProviderNotAvailable() {
			}

			@Override
			public void onError(String error) {
			}
		});
		
	}
	
	public void saveComment(final Comment comment, final DefaultSaveCallback<Comment> saveCallback)
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
							Log.d(TAG,"Saving: Parent Id.");
							saveObject.put(PARENT_ID, object);
							saveObject.saveInBackground(new CommentSaveCallback(saveCallback, saveObject));
						}
						@Override
						public void onProviderNotAvailable() {
						}

						@Override
						public void onError(String error) {
							
							saveCallback.onError(error);
							Log.e(TAG,"getParseObjectByPlaceId: "+error);
						}
					});
					
				}
				else
				{
					Log.d(TAG,"Saving: no Parent Id.");
					saveObject.saveInBackground(new CommentSaveCallback(saveCallback, saveObject));
				}
			}

			@Override
			public void onProviderNotAvailable() {
			}

			@Override
			public void onError(String error) {
				saveCallback.onError(error.toString());
				Log.e(TAG,error);
			}
		});
	}
	
	
	public void deleteVote(final Vote vote)
	{
		
		if(vote == null){
			Log.e(TAG,"Cannot delete null Vote.");
			return;
		}
		
		
		this.getCommentById(vote.getCommentId(), new DefaultFindFirstCallback<ParseObject>(){

			@Override
			public void onComplete(final ParseObject object) {
				if(object == null)
				{
					Log.e(TAG,"Error changing score.");
					return;
				}
				
				double score = object.getDouble(SCORE);
				
				if(vote.isUpvote())
				{
					score--;
				}
				else
				{
					score++;
				}
				object.put(SCORE, score);
				Log.d(TAG,"updated Score: "+object.getObjectId()+" to "+score);
				object.saveEventually();
			}

			@Override
			public void onProviderNotAvailable() {
			}

			@Override
			public void onError(String error) {
				Log.e(TAG,"Unable to retrieve Comment Parse Object: "+error);
			}
			
			
		});
		Log.d(TAG,"Deleting Vote: "+vote.getObjectId());
		
		
		ParseObject object = cacheManager.getVoteByCommentId(vote.getCommentId());
		
		cacheManager.removeVoteParseObject(object);
		if(object != null)
		{
			object.deleteEventually();
			return;
		}
		
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(VOTE_TABLE);
		query.whereEqualTo(VOTE_OBJECT_ID, vote.getObjectId());
		query.getFirstInBackground(new GetCallback<ParseObject>(){

			@Override
			public void done(ParseObject parseObject, ParseException e) {
				if(e != null)
				{
					Log.e(TAG,"Could not delete vote: ",e);
					return;
				}
				else if(parseObject != null)
				{
					parseObject.deleteEventually();
				}
			}
			
		});
		
	}
	
	
	private void setVotesForUser()
	{	
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
					cacheManager.addVoteParseObject(currentVote);
				}
				
			}
			
			
		});
	}
	
	/**
		Updates Cache for a collection of comments.
	*/
	
	
	
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
		
			Log.d(TAG,"Comment Saved.");
			
			cacheManager.addCommentParseObject(parseComment, null);
			
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
			
			ArrayList<Comment> comments = new ArrayList<Comment>();
			for(int i=0;i<list.size();i++)
			{
				ParseObject VoteObject = cacheManager.getVoteByCommentId(list.get(i).getObjectId());

				cacheManager.addCommentParseObject(list.get(i), null);
				Comment comment = ParseHelper.parseObjectToComment(list.get(i),VoteObject);
				comments.add(comment);
				
			}
			
			callback.onComplete(comments);
		}
		
	}
}
