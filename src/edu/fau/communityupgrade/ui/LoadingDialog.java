package edu.fau.communityupgrade.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.WindowManager;
import edu.fau.communityupgrade.R;

public class LoadingDialog extends ProgressDialog {
	
	//TODO: Put these in XML file. Load from there.
	public static final int DEFAULT_LOADING_TITLE_ID = R.string.logging_in_progress_title;
	public static final int DEFAULT_LOADING_MESSAGE_ID = R.string.logging_in_progress_message;
	
	public LoadingDialog(final Context context)
	{
		super(context);
		
		//Use default loading values
		setTitle(context.getString(DEFAULT_LOADING_TITLE_ID));
		setMessage(context.getString(DEFAULT_LOADING_MESSAGE_ID));
		setCancelable(false);

	}
	
	public LoadingDialog(final Context context, final String title, final String message )
	{
		super(context);
		
		setTitle(title);
		setMessage(message);
		
	}
}
