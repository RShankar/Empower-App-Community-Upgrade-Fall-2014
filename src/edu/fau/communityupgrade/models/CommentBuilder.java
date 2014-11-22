package edu.fau.communityupgrade.models;

import java.util.Date;

public class CommentBuilder
{
	private Comment comment;
	
	public CommentBuilder()
	{
		comment = new Comment();
	}
	
	public CommentBuilder setObjectId(String id)
	{
		comment.setObjectId(id);
		return this;
	}
	
	public CommentBuilder setContent(String content)
	{
		comment.setComment_content(content);
		return this;
	}
	
	public CommentBuilder setPlaceId(String id)
	{
		comment.setPlaceId(id);
		return this;
	}
	
	public CommentBuilder setParentId(String id)
	{
		comment.setParentId(id);
		return this;
	}
	public CommentBuilder setCreatedBy(User u)
	{
		comment.setCreatedBy(u);
		return this;
	}
	public CommentBuilder setCreatedAt(Date d)
	{
		comment.setCreatedAt(d);
		return this;
	}
	public CommentBuilder setScore(double s)
	{
		comment.setScore(s);
		return this;
	}
	public CommentBuilder setUserVote(Vote v)
	{
		comment.setUserVote(v);
		return this;
	}
	
	public Comment build()
	{
		return comment;
	}
}