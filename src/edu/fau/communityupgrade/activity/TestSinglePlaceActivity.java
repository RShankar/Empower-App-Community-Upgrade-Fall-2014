package edu.fau.communityupgrade.activity;



import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.fau.communityupgrade.R;
import edu.fau.communityupgrade.callback.DefaultSaveCallback;
import edu.fau.communityupgrade.database.CommentManager;
import edu.fau.communityupgrade.database.UserManager;
import edu.fau.communityupgrade.models.Comment;
import edu.fau.communityupgrade.models.Place;
import edu.fau.communityupgrade.models.User;
import edu.fau.communityupgrade.ui.LoadingDialog;

public class TestSinglePlaceActivity extends BaseActivity {

	//Key to retrieve Place from
	public static final String PLACE_OBJECT_EXTRA = "edu.fau.communityupgrade.activity.TestSinglePlaceActivity.PlaceObjectExtra";
	
	private static final String TAG = "TestSinglePlaceActivity";
	private Place place;
	private ListView commentListView;
	private ArrayList<Comment> comments;
	private CommentsAdapter adapter;
	private LoadingDialog loadingDialog;
	private Comment selectedComment;
	private Comment parentComment;
	private CommentManager commentManager;
	private View selectedView;
	private Button addCommentButton;
	private EditText addCommentContent;
	
	@Override
	public void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);
		selectedView = null;
		loadingDialog = new LoadingDialog(this);
		commentManager = new CommentManager(this);
		setContentView(R.layout.testplace_single_layout);
		place = (Place)getIntent().getParcelableExtra(PLACE_OBJECT_EXTRA);
		commentListView = (ListView)findViewById(R.id.test_comments_list_view);
		selectedComment = null;
		parentComment = null;
		comments = new ArrayList<Comment>();
		addCommentButton = (Button)findViewById(R.id.add_comment_button);
		addCommentContent = (EditText)findViewById(R.id.add_comment_content);
		
		addCommentButton.setOnClickListener(new AddCommentOnClickListener());
		adapter = new CommentsAdapter(this,comments);
		commentListView.setAdapter(adapter);
		
		adapter.addAll(place.getComments());
		commentListView.setOnItemClickListener(new CommentOnItemClickListener());
		
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	private class CommentOnItemClickListener implements OnItemClickListener
	{
		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, int position,
				long id) {
			
			view.setBackgroundColor(Color.BLUE);
			selectedComment = comments.get(position);
			/*loadingDialog.show();
			 * commentManager.getChildComments(comments.get(position), 
					new DefaultFindCallback<Comment>(){
				@Override
				public void onComplete(final ArrayList<Comment> list) {
					loadingDialog.dismiss();
					
					ListView childListView = new ListView(TestSinglePlaceActivity.this);
					childListView.setId(R.id.childListView);
					ArrayList<Comment> childComments = new ArrayList<Comment>();
					CommentsAdapter childAdapter = new CommentsAdapter(TestSinglePlaceActivity.this,childComments);
					childListView.setAdapter(childAdapter);
					
					childListView.setOnItemClickListener(new CommentOnItemClickListener());
					RelativeLayout mainLayout = (RelativeLayout)view.findViewById(R.id.comment_layout);
					ListView child = (ListView)mainLayout.findViewById(R.id.childListView);
					
					if(child != null)
					{
						mainLayout.removeView(child);
					}
					if(selectedView != null)
					{
						selectedView.setBackgroundColor(Color.TRANSPARENT);
					}
					
					mainLayout.addView(childListView);
					
					selectedView = mainLayout;
					mainLayout.setBackgroundColor(Color.BLUE);
					((TextView)view.findViewById(R.id.comment_content_text)).setText(list.toString());
					adapter.notifyDataSetChanged();
					Toast.makeText(TestSinglePlaceActivity.this, "Comments Retrieved: "+list.toString(), Toast.LENGTH_LONG).show();
				}

				@Override
				public void onProviderNotAvailable() {
				}
				
				@Override
				public void onError(String error) {
					loadingDialog.dismiss();
					Toast.makeText(TestSinglePlaceActivity.this, "Error getting comments", Toast.LENGTH_LONG).show();	
				}
			});*/
			
		}

	}
	
	private class AddCommentOnClickListener implements OnClickListener
	{


		@Override
		public void onClick(View v) {
			String commentContent = addCommentContent.getText().toString();
			User createdBy = UserManager.getInstance().getCurrentUser();
			String parentId = null;
			if(selectedComment != null)
				parentId = selectedComment.getObjectId();
			Comment comment = new Comment(null, commentContent,place.getObjectId(),createdBy, parentId, 0.0);
			commentManager.saveComment(comment, new DefaultSaveCallback<Comment>(){

				@Override
				public void onSaveComplete(Comment arg0) {
					addCommentContent.setText("");
					Toast.makeText(TestSinglePlaceActivity.this, "Comment Saved!", Toast.LENGTH_LONG).show();
					
				}

				@Override
				public void onProviderNotAvailable() {
				}

				@Override
				public void onError(String error) {
					Toast.makeText(TestSinglePlaceActivity.this, "Comment Saved!", Toast.LENGTH_LONG).show();
				}
				
				
			});
			
		}
		
	}
	
	
	/**
	 * This Adapter is used to list each comment on the list view in the layout
	 * @author kyle
	 *
	 */
	private class CommentsAdapter extends ArrayAdapter<Comment>
	{
		public CommentsAdapter(Context context, ArrayList<Comment> objects) {
			super(context, 0, objects);
		}
		
		@Override
		public View getView(int position,View convertView, ViewGroup parent)
		{
			//Get data
			Comment comment = getItem(position);
			// Check if an existing view is being reused, otherwise inflate the view
		    if (convertView == null) {
		    	convertView = LayoutInflater.from(getContext()).inflate(R.layout.testplace_single_comment_item, parent, false);
		    }
		    
		    TextView comment_content = (TextView)convertView.findViewById(R.id.comment_content_text);
		    TextView score = (TextView)convertView.findViewById(R.id.comment_score);
		    comment_content.setText(comment.getComment_content());
		    score.setText(""+comment.getScore());
		    
		    return convertView;
		}
	}
}
