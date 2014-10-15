package edu.fau.communityupgrade.helper;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import edu.fau.communityupgrade.database.CommentManager;
import edu.fau.communityupgrade.database.PlaceManager;
import edu.fau.communityupgrade.database.UserManager;
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
		
		User user = new User(parseUser.getString(UserManager.USERNAME),
				parseUser.getObjectId());	
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
		
		User createdBy = null;
		
		//if(parseObject.containsKey(CommentManager.CREATED_BY))
			//createdBy = parseUserToUser(parseObject.getParseUser(CommentManager.CREATED_BY));
		
		String parentId = null;
		//if(parseObject.containsKey(CommentManager.PARENT_ID))
		 //parentId = parseObject.getParseObject(CommentManager.PARENT_ID).getObjectId();
		
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
		ParseUser parseUser = parseObject.getParseUser(PlaceManager.CREATED_BY);
		User user = parseUserToUser(parseUser);
		
		String name = parseObject.getString(PlaceManager.NAME);
		
		
		
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
		Place place = new Place(name,user,point.getLatitude(),point.getLongitude(),null);
		
		return place;
	}
	
	
	
}
