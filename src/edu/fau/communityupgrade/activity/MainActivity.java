package edu.fau.communityupgrade.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import edu.fau.communityupgrade.R;
import edu.fau.communityupgrade.callback.DefaultFindCallback;
import edu.fau.communityupgrade.database.PlaceManager;
import edu.fau.communityupgrade.models.Place;
import edu.fau.communityupgrade.ui.LoadingDialog;


public class MainActivity extends BaseActivity {

	private PlaceManager placeManager;
	private LoadingDialog mProgressDialog;
	
	private static final String TAG = "MainActivity";
	private TextView textView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mProgressDialog = new LoadingDialog(this);
		textView = (TextView)findViewById(R.id.basic_text_view);
	}
	
	@Override
	public void onResume()
	{	
		super.onResume();
		Intent intent = new Intent(MainActivity.this,TestPlaceActivity.class);
		startActivity(intent);
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
