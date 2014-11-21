package edu.fau.communityupgrade.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.util.Log;

import com.parse.ParseObject;

public class CacheManager {
	private static final String TAG = "CacheManager";
	
	private static CacheManager cacheManager;
	
	private HashSet<String> placeObjectIdsNearUser;
	private HashMap<String,HashSet<String>> commentIdsByPlaceId;
	private HashMap<String,HashSet<String>> commentIdsByParentId;
	private HashMap<String,String> voteStatusIdByCommentId;
	private HashMap<String,ParseObject> parseObjectById;
	
	
	private CacheManager()
	{
		commentIdsByPlaceId = new HashMap<String,HashSet<String>>();
		commentIdsByParentId = new HashMap<String,HashSet<String>>(); 
		parseObjectById = new HashMap<String,ParseObject>();
		placeObjectIdsNearUser = new HashSet<String>();
		voteStatusIdByCommentId = new HashMap<String,String>();
	}
	
	public static CacheManager getInstance()
	{
		Log.d(TAG,"Creating Cache Manager.");
		if(cacheManager == null)
		{
			
			cacheManager = new CacheManager(); 
		}
		
		return cacheManager;
	}

	public boolean containsParseObjectId(String id)
	{
		return parseObjectById.containsKey(id);
	}
	
	public ParseObject getParseObjectById(String id)
	{
		return parseObjectById.get(id);
	}
	
	
	
	//COMMENTS
	
	public boolean hasCommentsForPlaceId(final String id)
	{
		return commentIdsByPlaceId.containsKey(id);
	}
	
	/**
	 * Returns null if not set
	 * @param id 
	 * @return
	 */
	public List<ParseObject> getCommentsByPlaceId(final String id)
	{
		
		HashSet<String> ids = commentIdsByPlaceId.get(id);
		
		if(ids == null)
			return null;
		
		String[] idArray =  Arrays.asList(ids.toArray()).toArray(new String[ids.toArray().length]);
		
		List<ParseObject> comments = new ArrayList<ParseObject>();
		
		for(int i=0;i<ids.size();i++)
		{
			final ParseObject comment = parseObjectById.get(idArray[i]);
			comments.add(comment);
		}
		
		return comments;
	}
	
	public boolean containsNearbyPlaces()
	{
		return placeObjectIdsNearUser.isEmpty() == false;
	}
	
	public ArrayList<ParseObject> getNearbyPlaces()
	{
		final ArrayList<ParseObject> places = new ArrayList<ParseObject>();
		
		String[] ids = Arrays.asList(placeObjectIdsNearUser.toArray()).toArray(new String[placeObjectIdsNearUser.toArray().length]);
		
		for(int i=0;i<ids.length;i++)
		{
			ParseObject pObject = parseObjectById.get(ids[i]);
			places.add(pObject);
		}
		return places;
	}
	
	public boolean hasVoteByCommentId(final String commentId)
	{
		return voteStatusIdByCommentId.containsKey(commentId);
	}
	
	public ParseObject getVoteByCommentId(final String commentId)
	{
		String objectId = voteStatusIdByCommentId.get(commentId);
		
		if(objectId == null)
			return null;
		return parseObjectById.get(objectId);
		
	}
	
	public void removeVoteParseObject(final ParseObject parseObject)
	{
		if(parseObject == null || parseObject.getObjectId() == null)
		{
			Log.e(TAG,"removeVote: Invalid Object.");
			return;
		}
		
		this.parseObjectById.remove(parseObject.getObjectId());
		this.voteStatusIdByCommentId.remove(parseObject.getString(CommentManager.VOTE_COMMENT_ID));
	}
	
	public void addVoteParseObject(final ParseObject parseObject)
	{
		if(parseObject == null)
		{
			//Log.e(TAG,"addVoteParseObject: Invalid Object");
			return;
		}
		
		parseObjectById.put(parseObject.getObjectId(), parseObject);
		voteStatusIdByCommentId.put(parseObject.getString(CommentManager.VOTE_COMMENT_ID), parseObject.getObjectId());
		
	}
	
	public boolean hasCommentChildObjects(final String parentId)
	{
		return commentIdsByParentId.containsKey(parentId);
	}
	
	public List<ParseObject> getCommentChildObjects(final String parentId)
	{
		HashSet<String> set = commentIdsByParentId.get(parentId);
		
		if(set == null)
			return null;
		
		
		
		List<ParseObject> objects = new ArrayList<ParseObject>();
		
		for(int i=0;i<set.size();i++)
		{
			objects.add(this.getParseObjectById((String)set.toArray()[i]));
		}
		
		return objects;
	}
	
	public void addCommentParseObject(final List<ParseObject> objects)
	{
		for(int i=0;i<objects.size();i++)
			addCommentParseObject(objects.get(i),null);
	}
	public void addCommentParseObject(final ParseObject commentObject, final ParseObject voteObject)
	{
		if(commentObject == null || commentObject.getObjectId() == null)
		{
			Log.e(TAG,"addParseObject: Invalid Object");
			return;
		}
		//Log.d(TAG,"Add Comment: "+commentObject.getObjectId());
		
		final String placeId = commentObject.getParseObject(CommentManager.PLACE_ID).getObjectId();
		
		parseObjectById.put(commentObject.getObjectId(), commentObject);
		
		if(!this.commentIdsByPlaceId.containsKey(placeId))
		{
			Log.d(TAG,"Adding placeId HashSet: "+placeId);
			commentIdsByPlaceId.put(placeId, new HashSet<String>());
		}
		commentIdsByPlaceId.get(placeId).add(commentObject.getObjectId());
		
		if(commentObject.has(CommentManager.PARENT_ID))
		{
			final String parentId = commentObject.getString(CommentManager.PARENT_ID);
			Log.d(TAG,"Parent Id: "+parentId);
			if(!this.commentIdsByParentId.containsKey(parentId))
				this.commentIdsByParentId.put(parentId, new HashSet<String>());
			this.commentIdsByParentId.get(parentId).add(commentObject.getObjectId());
		}
		
		addVoteParseObject(voteObject);
	}
	public void addEmptyCommentsForPlaceId(final String placeId)
	{
		commentIdsByPlaceId.put(placeId, new HashSet<String>());
	}
	public void addEmptyCommentsForParentId(final String parentId)
	{
		commentIdsByParentId.put(parentId, new HashSet<String>());
	}

	public void addPlaceParseObject(final ParseObject object)
	{
		if(object == null || object.getObjectId() == null)
		{
			Log.e(TAG,"addParseObject: Invalid Object.");
			return;
		}
		parseObjectById.put(object.getObjectId(), object);
		
	}
	
	public void addPlaceParseObjectListNearby(final List<ParseObject> objects)
	{
		placeObjectIdsNearUser.clear();
		for(int i=0;i<objects.size();i++){
			addPlaceParseObjectNearby(objects.get(i));
		}
	}
	
	public void addPlaceParseObjectNearby(final ParseObject object)
	{
		if(object == null || object.getObjectId() == null)
		{
			Log.e(TAG,"Error loggin cache");
			return;
		}
		
		addPlaceParseObject(object);
		placeObjectIdsNearUser.add(object.getObjectId());
	}
	
	public void clearCache()
	{
		this.commentIdsByParentId.clear();
		this.commentIdsByPlaceId.clear();
		this.placeObjectIdsNearUser.clear();
		this.voteStatusIdByCommentId.clear();
		this.parseObjectById.clear();
	}
}
