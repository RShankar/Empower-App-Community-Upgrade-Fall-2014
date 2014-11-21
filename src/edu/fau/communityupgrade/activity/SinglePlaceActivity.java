package edu.fau.communityupgrade.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
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
	
	private final int COLOR_TRANSPARENT = Color.TRANSPARENT;
	private final int COLOR_COMMENT_SELECTED = Color.parseColor("#def0fd");
	
	private final int ARROW_UP = R.drawable.arrow_up2;
	private final int ARROW_UP_SELECTED = R.drawable.arrow_up_selected;
	private final int ARROW_DOWN = R.drawable.arrow_down2;
	private final int ARROW_DOWN_SELECTED = R.drawable.arrow_down_selected;
	
	private Place currentPlace;
	private CommentManager commentManager; 
	private LoadingDialog savingDialog;
	private LoadingDialog loadingDialog;
	private AlertDialog replyCommentDialog;
	private AlertDialog addCommentDialog;
	
	private TextView placeTitle;
	private ListView commentListView;
	private View selectedCommentView;
	private CommentAdapterItem selectedCommentItem;
	private ArrayList<CommentAdapterItem> commentItemArray;
	private CommentsAdapter commentsAdapter;
	
	
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
		savingDialog = new LoadingDialog(this,"Saving Comment","Saving Comment.");
		loadingDialog = new LoadingDialog(this);
		
		//Load Views
		commentListView = (ListView)findViewById(R.id.comment_list_view);
		placeTitle = (TextView)findViewById(R.id.single_place_title);
		placeTitle.setText(currentPlace.getName());
		
		commentItemArray = CommentAdapterItem.commentListToItemList(currentPlace.getComments(),null);
		commentListView.setOnItemClickListener(new CommentItemClickListener());	
		
		commentsAdapter = new CommentsAdapter(this, commentItemArray);
		commentListView.setAdapter(commentsAdapter);
		
		replyCommentDialog = buildCommentAddDialog("Reply To Comment","Reply","Cancel");
		addCommentDialog = buildCommentAddDialog("Add Comment","Add Comment", "Cancel");
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.comment_activity_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_add_comment:
	        	addComment();
	            return true;
	        case R.id.action_settings:
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
		
	@Override
	public void onResume()
	{
		super.onResume();
		
		
	}
	
	private void addComment()
	{
		addCommentDialog.show();
	}
	
	private void replyToComment()
	{
		replyCommentDialog.show();
	}
	
	private AlertDialog buildCommentAddDialog(final String title, final String positiveBtn, final String negButton)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(title);

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setLines(5);
		input.setSingleLine(false);
		input.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
		input.setGravity(Gravity.TOP);
		alert.setView(input);

		alert.setPositiveButton(positiveBtn, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  String value = input.getText().toString();
		  Log.d(TAG,"saving.");
		  saveComment(value);
		  input.getText().clear();
		  }
		});

		alert.setNegativeButton(negButton, new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
			  input.getText().clear();
		  }
		});
		
		return alert.create();
		
	}
	
	private void saveComment(final String comment_content){
		Log.d(TAG,"saveComment.");
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
		
		savingDialog.show();
		commentManager.saveComment(comment, new DefaultSaveCallback<Comment>(){

			@Override
			public void onSaveComplete(final Comment c) {
				Log.d(TAG,"onSaveComplete.");
				savingDialog.dismiss();
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
				savingDialog.dismiss();
				Log.e(TAG,"onError");
				Toast.makeText(SinglePlaceActivity.this, getString(R.string.add_comment_saved_error), 
						Toast.LENGTH_LONG).show();
			}
		});
	}
	
	
	
	private class CommentItemClickListener implements OnItemClickListener
	{
		
		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position,
				long id) {
			Log.d(TAG,"item selected");
			
			final ImageView upvoteButton = (ImageView)view.findViewById(R.id.upvote_btn);
		    final ImageView downvoteButton = (ImageView)view.findViewById(R.id.downvote_btn);
			final ImageView replyButton = (ImageView)view.findViewById(R.id.comment_reply);
			if(selectedCommentView == view)
			{
				selectedCommentView.setBackgroundColor(COLOR_TRANSPARENT);
				selectedCommentItem.removeAllChildViews(commentsAdapter);
				
			    upvoteButton.setVisibility(View.GONE);
			    downvoteButton.setVisibility(View.GONE);
				selectedCommentView = null;
				selectedCommentItem = null;
				commentsAdapter.notifyDataSetChanged();
				return;
			}
			if(selectedCommentView != null)
			{
				selectedCommentView.setBackgroundColor(COLOR_TRANSPARENT);
				final ImageView selectedUpvoteButton = (ImageView)selectedCommentView.findViewById(R.id.upvote_btn);
			    final ImageView selectedDownvoteButton = (ImageView)selectedCommentView.findViewById(R.id.downvote_btn);
			    final ImageView selectedReplyButton = (ImageView)selectedCommentView.findViewById(R.id.comment_reply);
			    selectedReplyButton.setVisibility(View.GONE);
			    selectedUpvoteButton.setVisibility(View.GONE);
			    selectedDownvoteButton.setVisibility(View.GONE);
			}
			replyButton.setVisibility(View.VISIBLE);
			upvoteButton.setVisibility(View.VISIBLE);
		    downvoteButton.setVisibility(View.VISIBLE);
			view.setBackgroundColor(COLOR_COMMENT_SELECTED);
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
									loadingDialog.dismiss();				
								}
					});
			}	
		}
	}
	
	/**
	 * This Adapter is used to list each comment on the list view in the layout
	 * @author kyle
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
			final ImageView replyButton = (ImageView)convertView.findViewById(R.id.comment_reply);
			
			replyButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					replyToComment();
				}
				
			});
			
			scoreView.setText(""+comment.getScore());
		    
		    final TextView commentContent = (TextView)convertView.findViewById(R.id.comment_content);
		    commentContent.setText(comment.getComment_content());
		    
		    final TextView commentUserName = (TextView)convertView.findViewById(R.id.comment_by);
		    
		    commentUserName.setText(comment.getCreatedBy().getUsername());
		    
		    final ImageView upvoteButton = (ImageView)convertView.findViewById(R.id.upvote_btn);
		    final ImageView downvoteButton = (ImageView)convertView.findViewById(R.id.downvote_btn);
		    
		    
		    if(convertView != selectedCommentView){
		    	upvoteButton.setVisibility(View.GONE);
		    	downvoteButton.setVisibility(View.GONE);
		    	replyButton.setVisibility(View.GONE);
		    }	
		    
		    if(!vote.isSet())
		    {
		    	upvoteButton.setImageResource(ARROW_UP);
		    	downvoteButton.setImageResource(ARROW_DOWN);
		    }	
		    else if(vote.isUpvote())
		    {
		    	upvoteButton.setImageResource(ARROW_UP_SELECTED);
		    	downvoteButton.setImageResource(ARROW_DOWN);
		    }
		    else
		    {
		    	upvoteButton.setImageResource(ARROW_UP);
		    	downvoteButton.setImageResource(ARROW_DOWN_SELECTED);
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
		    			downvoteButton.setImageResource(ARROW_DOWN_SELECTED);
						upvoteButton.setImageResource(ARROW_UP);
						score--;
		    		}
		    		else
		    		{
		    			
		    			commentManager.deleteVote(vote);
		    			vote.removeVote();
		    			score++;
		    			downvoteButton.setImageResource(ARROW_DOWN);
		    		}

					upvoteButton.setFocusable(false);
					downvoteButton.setFocusable(false);
					comment.setScore(score);
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
						
						upvoteButton.setImageResource(ARROW_UP_SELECTED);
						vote.setVoteType(true);
						downvoteButton.setImageResource(ARROW_DOWN);
						
						score++;
						
					}
					else
					{ 
						commentManager.deleteVote(vote);
		    			vote.removeVote();
		    			score--;
						upvoteButton.setImageResource(ARROW_UP);
					}

					upvoteButton.setFocusable(false);
					downvoteButton.setFocusable(false);
					comment.setScore(score);
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
