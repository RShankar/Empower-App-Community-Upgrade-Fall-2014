package edu.fau.communityupgrade.helper;

import java.util.ArrayList;
import java.util.List;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import edu.fau.communityupgrade.database.CommentManager;
import edu.fau.communityupgrade.database.PlaceManager;
import edu.fau.communityupgrade.models.Comment;
import edu.fau.communityupgrade.models.Place;
import edu.fau.communityupgrade.models.User;

/**
 * This Helper class is used to convert ParseObjects 
 * to Object Models.
 * @author kyle
 *
 */
public class ParseHelper {

	
	/**
	 * Converts ParseUser to User Model Object
	 * @param parseUser
	 * @return
	 */
	public static User parseUserToUser(final ParseUser parseUser)
	{
		User user = new User(parseUser.getUsername(),parseUser.getObjectId());	
		return user;
	}
	
	/**
	 * Converts ParseObject to Comment Model Object
	 * @param parseObject
	 * @return
	 */
	public static Comment parseObjectToComment(final ParseObject parseObject)
	{
		String objectId = parseObject.getObjectId();
		String comment_content = parseObject.getString(CommentManager.COMMENT_CONTENT);
		String placeId = parseObject.getParseObject(CommentManager.PLACE_ID).getObjectId();
		User createdBy = parseUserToUser(parseObject.getParseUser(CommentManager.CREATED_BY));
		String parentId = parseObject.getParseObject(CommentManager.PARENT_ID).getObjectId();
		
		Comment comment = new Comment(objectId,comment_content,placeId,createdBy,parentId);
		return comment;
	}
	
	/**
	 * Converts a ParseObject to a Place Model Object
	 * @param parseObject
	 * @return
	 */
	public static Place parseObjectToPlace(final ParseObject parseObject)
	{
		String name = parseObject.getString(PlaceManager.NAME);
		
		//User user = parseUserToUser(parseObject.getParseUser(PlaceManager.CREATED_BY));
		
		ParseGeoPoint point = parseObject.getParseGeoPoint(PlaceManager.GEOPOINT);
		
		/*final ArrayList<Comment> comments = new ArrayList<Comment>();
		
		ParseRelation<ParseObject> relation = parseObject.getRelation(CommentManager.TABLE);
		ParseQuery<ParseObject> query = relation.getQuery();
		
		query.findInBackground(new FindCallback<ParseObject>(){
			@Override
			public void done(List<ParseObject> list, ParseException arg1) {
				
				for(int i=0;i<list.size();i++)
				{
					comments.add(parseObjectToComment(list.get(i)));
				}
			}
			
		});
		*/
		//Create Place
		Place place = new Place(name,null,point.getLatitude(),point.getLongitude(),null);
		
		return place;
	}
	
	
	
}
