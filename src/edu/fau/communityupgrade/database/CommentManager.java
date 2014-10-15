package edu.fau.communityupgrade.database;

import java.util.ArrayList;
import java.util.List;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import edu.fau.communityupgrade.helper.ParseHelper;
import edu.fau.communityupgrade.models.Comment;

public class CommentManager {
	
	public static String TABLE = "Comment";
	
	public static String OBJECT_ID = "objectId";
	public static String COMMENT_CONTENT = "comment_content";
	public static String PLACE_ID = "placeId";
	public static String CREATED_BY = "createdBy";
	public static String PARENT_ID = "parentId";
	
	
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
		
		query.whereEqualTo(PLACE_ID, parseObject);
		query.whereEqualTo(PARENT_ID, null);
		List<ParseObject> parseObjects;
		
		try {
			parseObjects = query.find();
		} 
		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return null;
		}
		
		ArrayList<Comment> list = new ArrayList<Comment>();
		
		for(int i=0;i<parseObjects.size();i++)
		{
			list.add(ParseHelper.parseObjectToComment(parseObjects.get(i)));
		}
		
		
		
		return list;
	}
}
