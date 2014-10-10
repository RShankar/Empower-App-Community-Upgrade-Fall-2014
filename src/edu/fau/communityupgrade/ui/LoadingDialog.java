package edu.fau.communityupgrade.ui;

import android.app.ProgressDialog;
import android.content.Context;
import edu.fau.communityupgrade.R;

public class LoadingDialog {

	final Context context;
	final ProgressDialog mProgressDialog;
	
	public LoadingDialog(Context context)
	{
		this.context = context;
		mProgressDialog = new ProgressDialog(context);
		
		mProgressDialog.setTitle(context.getString(R.string.logging_in_progress_title));
		mProgressDialog.setMessage(context.getString(R.string.logging_in_progress_message));
	}
	
	public void show()
	{
		mProgressDialog.show();
	}
	
	public void dismiss()
	{
		mProgressDialog.dismiss();
	}
}
