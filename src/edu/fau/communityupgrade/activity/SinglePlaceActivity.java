package edu.fau.communityupgrade.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.fau.communityupgrade.R;
import edu.fau.communityupgrade.callback.DefaultFindCallback;
import edu.fau.communityupgrade.callback.DefaultSaveCallback;
import edu.fau.communityupgrade.database.CommentManager;
import edu.fau.communityupgrade.database.UserManager;
import edu.fau.communityupgrade.models.Comment;
import edu.fau.communityupgrade.models.Place;
import edu.fau.communityupgrade.models.User;
import edu.fau.communityupgrade.models.Vote;
import edu.fau.communityupgrade.ui.LoadingDialog;

public class SinglePlaceActivity extends BaseActivity {

	public static final String PLACE_OBJECT_EXTRA_KEY = "SinglePLaceActivity.PlaceObjectExtra";
	private static final String TAG = "SinglePlaceActivity";
	
	
	private Place currentPlace;
	private CommentManager commentManager; 
	private LoadingDialog loadingDialog;
	private ListView commentListView;
	private View selectedCommentView;
	private CommentAdapterItem selectedCommentItem;
	private ArrayList<CommentAdapterItem> commentItemArray;
	private CommentsAdapter commentsAdapter;
	private EditText commentText;
	private Button AddCommentButton;
	
	@Override
	public void onCreate(final Bundle savedInstance)
	{
		super.onCreate(savedInstance);
		setContentView(R.layout.place_single_layout);
		currentPlace = (Place)getIntent().getParcelableExtra(PLACE_OBJECT_EXTRA_KEY);
		commentManager = new CommentManager(this);
		selectedCommentView = null;
		selectedCommentItem = null;
		
		if(currentPlace == null)
		{
			Log.e(TAG,"Place is Null. Exiting Activity");
			this.finish();
		}

		//Initialize Loading Dialog
		loadingDialog = new LoadingDialog(this,"Saving Comment","Saving Comment.");
		
		//Load Views
		commentText = (EditText)findViewById(R.id.add_comment_content);
		AddCommentButton = (Button)findViewById(R.id.add_comment_button);
		commentListView = (ListView)findViewById(R.id.comment_list_view);
		commentItemArray = CommentAdapterItem.commentListToItemList(currentPlace.getComments(),null);
		commentListView.setOnItemClickListener(new CommentItemClickListener());
		AddCommentButton.setOnClickListener(new AddCommentClickListener());	
		
		commentsAdapter = new CommentsAdapter(this, commentItemArray);
		commentListView.setAdapter(commentsAdapter);
		
	}
	
	/**
	 * OnClickListener Implementation for AddCommentButton.
	 * Adds the comment to the database.
	 * @author kyle
	 *
	 */
	private final class AddCommentClickListener implements OnClickListener
	{
		
		@Override
		public void onClick(View v) {
			String comment_content = commentText.getEditableText().toString();
			
			if(comment_content.isEmpty())
			{
				Toast.makeText(SinglePlaceActivity.this, getString(R.string.add_comment_validation_empty_error), 
						Toast.LENGTH_LONG).show();
				return;
			}
			if(comment_content.length() < 3)
			{
				Toast.makeText(SinglePlaceActivity.this, getString(R.string.add_comment_validation_short_error), 
						Toast.LENGTH_LONG).show();
				return;
			}
			
			User currentUser = UserManager.getInstance().getCurrentUser();
			Comment parentComment = null;
			String parentId = null;
			if(selectedCommentItem != null)
			{
				parentComment = selectedCommentItem.getComment();
				parentId = parentComment.getObjectId();
			}
			
			Comment comment = new Comment(null, comment_content, currentPlace.getObjectId(), currentUser, parentId, 0,null);
			
			loadingDialog.show();
			commentManager.saveComment(comment, new DefaultSaveCallback<Comment>(){

				@Override
				public void onSaveComplete(final Comment c) {
					loadingDialog.dismiss();
					commentText.getEditableText().clear();
					Log.d(TAG,c.toString());
					CommentAdapterItem item = new CommentAdapterItem(c,selectedCommentItem);
					int position = commentsAdapter.getPosition(selectedCommentItem);
					commentsAdapter.insert(item,position+1);
					if(selectedCommentItem != null)
					{
						selectedCommentItem.addChildView(item);
					}
					commentsAdapter.notifyDataSetChanged();
					Toast.makeText(SinglePlaceActivity.this, getString(R.string.add_comment_saved_confirmation), 
							Toast.LENGTH_LONG).show();
					
				}

				@Override
				public void onProviderNotAvailable() {
				}

				@Override
				public void onError(String error) {
					loadingDialog.dismiss();
					Toast.makeText(SinglePlaceActivity.this, getString(R.string.add_comment_saved_error), 
							Toast.LENGTH_LONG).show();
				}
			});
		}
	}
	
	
	private class CommentItemClickListener implements OnItemClickListener
	{
		
		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position,
				long id) {
			Log.d(TAG,"item selected");
			if(selectedCommentView == view)
			{
				selectedCommentView.setBackgroundColor(Color.TRANSPARENT);
				selectedCommentItem.removeAllChildViews(commentsAdapter);
				selectedCommentView = null;
				selectedCommentItem = null;
				commentsAdapter.notifyDataSetChanged();
				return;
			}
			if(selectedCommentView != null)
			{
				selectedCommentView.setBackgroundColor(Color.TRANSPARENT);
			}
			
			view.setBackgroundColor(Color.BLUE);
			loadingDialog.show();
			selectedCommentView = view;
			
			
			final CommentAdapterItem currentItem =  commentsAdapter.getItem(position);
			selectedCommentItem = currentItem;
			final Comment comment = currentItem.getComment();
			
			
			if(!currentItem.isChildViewsEmpty())
			{
				loadingDialog.dismiss();
			}
			else{
				
			//Load ChildViews if it's empty.
				commentManager.getChildComments(comment, new DefaultFindCallback<Comment>()
							{
								@Override
								public void onComplete(ArrayList<Comment> list) {
									loadingDialog.dismiss();
									final ArrayList<CommentAdapterItem> items = CommentAdapterItem.commentListToItemList(list,currentItem);
									int currentIndex = position+1;
									for(int i=0;i<items.size();i++)
									{
									    if(selectedCommentItem != null)
									    {
									    	selectedCommentItem.addChildView(items.get(i));
									    }
										commentsAdapter.insert(items.get(i), currentIndex);
										currentIndex++;
									}
								}
		
								@Override
								public void onProviderNotAvailable() {
								}
		
								@Override
								public void onError(String error) {
									// TODO Auto-generated method stub						
								}
					});
			}	
		}
	}
	
	/**
	 * This Adapter is used to list each comment on the list view in the layout
	 * @author kyle
	 *
	 */
	private class CommentsAdapter extends ArrayAdapter<CommentAdapterItem>
	{
		public CommentsAdapter(Context context, ArrayList<CommentAdapterItem> objects) {
			super(context, 0, objects);
		}
		
		@Override
		public View getView(int position,View convertView, ViewGroup parent)
		{
			//Get data
			final CommentAdapterItem currentItem = getItem(position);
			final Comment comment = currentItem.getComment();
			final Vote vote = comment.getUserVote();
			
			// Check if an existing view is being reused, otherwise inflate the view
		    if (convertView == null) {
		    	convertView = LayoutInflater.from(getContext()).inflate(R.layout.place_comment_list_item, parent, false);
		    }
		    convertView.setClickable(true);
		    View indents[] = {
		    		convertView.findViewById(R.id.left_indent1),
		    		convertView.findViewById(R.id.left_indent2),
		    		convertView.findViewById(R.id.left_indent3),
		    		convertView.findViewById(R.id.left_indent4),
		    		convertView.findViewById(R.id.left_indent5),
		    		convertView.findViewById(R.id.left_indent6),
		    		convertView.findViewById(R.id.left_indent7),
		    		convertView.findViewById(R.id.left_indent8)
		    };
		    
		    for(int i=0;i<8;i++)
		    {
		    	if(i < currentItem.getLevel()){
		    		indents[i].setVisibility(View.VISIBLE);
		    		indents[i].setBackgroundColor(Color.DKGRAY);
		    	}
		    	else
		    	{
		    		indents[i].setVisibility(View.GONE);
		    	}
		    }
		    
			final TextView scoreView = (TextView)convertView.findViewById(R.id.score);
			Log.d(TAG,comment.toString());
			scoreView.setText(""+comment.getScore());
		    
		    final TextView commentContent = (TextView)convertView.findViewById(R.id.comment_content);
		    commentContent.setText(comment.getComment_content()+" by "+comment.getCreatedBy().getUsername());
		    
		    final ImageButton upvoteButton = (ImageButton)convertView.findViewById(R.id.upvote_btn);
		    final ImageButton downvoteButton = (ImageButton)convertView.findViewById(R.id.downvote_btn);
		    
		    
		    final int transparent = Color.TRANSPARENT;
		    final int selectedVote = Color.parseColor("#dcdcdc");
		    
		    if(!vote.isSet())
		    {
		    	upvoteButton.setBackgroundColor(transparent);
		    	downvoteButton.setBackgroundColor(transparent);
		    }	
		    else if(vote.isUpvote())
		    {
		    	upvoteButton.setBackgroundColor(selectedVote);
		    	downvoteButton.setBackgroundColor(transparent);
		    }
		    else
		    {
		    	upvoteButton.setBackgroundColor(transparent);
		    	downvoteButton.setBackgroundColor(selectedVote);
		    }
		    
		    downvoteButton.setOnClickListener(new OnClickListener(){
		    	@Override
				public void onClick(View v) {
		    		
		    		double score = Double.parseDouble(scoreView.getText().toString());
		    		
		    		//Log.d(TAG,"VoteIsSet: "+vote.isSet()+" VoteIsUpvote: "+vote.isUpvote());
		    		if(!vote.isSet() || vote.isUpvote()){
		    			
		    			if(vote.isSet() && vote.isUpvote())
		    			{
		    				score--;
		    			}
		    			
		    			commentManager.addVote(comment.getObjectId(), false);
		    			vote.setVoteType(false);
		    			downvoteButton.setBackgroundColor(selectedVote);
						upvoteButton.setBackgroundColor(transparent);
						
						score--;
		    		}
		    		else
		    		{
		    			//TODO: Delete Vote
		    			vote.removeVote();
		    			score++;
		    			downvoteButton.setBackgroundColor(transparent);
		    		}

					scoreView.setText(""+score);
				}
		    	
		    });
		    
		    upvoteButton.setOnClickListener(new OnClickListener(){
		    	
				@Override
				public void onClick(View v) {
					double score = Double.parseDouble(scoreView.getText().toString());
					//Log.d(TAG,"VoteIsSet: "+vote.isSet()+" VoteIsUpvote: "+vote.isUpvote());
					if(!vote.isSet() || !vote.isUpvote()){
						
						if(vote.isSet() && !vote.isUpvote())
							score++;
						
						commentManager.addVote(comment.getObjectId(), true);
						upvoteButton.setBackgroundColor(selectedVote);
						vote.setVoteType(true);
						downvoteButton.setBackgroundColor(transparent);
						
						score++;
						
					}
					else
					{ 
						//TODO: Delete Vote
		    			vote.removeVote();
		    			score--;
						upvoteButton.setBackgroundColor(transparent);
					}
					
					scoreView.setText(""+score);
				}
		    });
		    
		    upvoteButton.setFocusable(false);
		    downvoteButton.setFocusable(false);
		    
		    return convertView; 
		}
	}

	/**
	 * This Is used to store all the informtation necessary for each Object in the ListView
	 * @author kyle
	 *
	 */
	private static class CommentAdapterItem 
	{
		private final Comment comment;
		private final CommentAdapterItem parentItem;
		private final int level;
		private final ArrayList<CommentAdapterItem> childViews;
		
		
		public CommentAdapterItem(final Comment c, final CommentAdapterItem p)
		{
			comment = c;
			childViews = new ArrayList<CommentAdapterItem>();
			parentItem = p;
			if(p != null)
			{
				level = (parentItem.getLevel()+1);
			}
			else{
				level = 0;
			}
		}
		public Comment getComment()
		{
			return comment;
		}
		
		public boolean isChildViewsEmpty()
		{
			return childViews.isEmpty();
		}
		
		public void addChildView(CommentAdapterItem child)
		{
			childViews.add(child);
		}
		public int getLevel()
		{
			return level;
		}
		
		public void removeAllChildViews(final ArrayAdapter<CommentAdapterItem> adapter)
		{
			for(int i=0;i<childViews.size();i++)
			{
				childViews.get(i).removeAllChildViews(adapter);
				adapter.remove(childViews.get(i));
			} 
			childViews.clear();
		}
		
		public static ArrayList<CommentAdapterItem> commentListToItemList(List<Comment> comments, CommentAdapterItem p)
		{
			ArrayList<CommentAdapterItem> items = new ArrayList<CommentAdapterItem>();
			if(comments == null)
				return items;
			for(int i=0;i<comments.size();i++)
			{
				CommentAdapterItem item = new CommentAdapterItem(comments.get(i),p);
				items.add(item);
			}
			
			return items;
		}
	}
	
}
