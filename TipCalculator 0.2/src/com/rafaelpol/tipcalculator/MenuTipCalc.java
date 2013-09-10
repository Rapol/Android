package com.rafaelpol.tipcalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MenuTipCalc extends ListActivity {

	private boolean onGoingTransition=false;
	private ArrayList<String> list;
	private MySimpleArrayAdapter adapter;
	static final int ADD_TO_LIST_REQUEST_CODE = 1;
	static final String WAITRESS_NAME="WAITRESS";
	static final String TAG= "MENUTIPCALC";
	private SharedPreferences prefs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		//				R.layout.rowlawyout,R.id.label, list);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		prefs = getSharedPreferences("list", MODE_PRIVATE);
		Set<String> set = new HashSet<String>();
		set = prefs.getStringSet("array", null);
		list = new ArrayList<String>();
		if(set!=null){
			list.addAll(set);
		}
		adapter = new MySimpleArrayAdapter(this,list);
		setListAdapter(adapter);
		final ListView listView = getListView();
		listView.setBackgroundColor(Color.BLACK);
		this.getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
				//Do some
				if(!onGoingTransition){
					onGoingTransition=true;
					final String item = (String) parent.getItemAtPosition(position);
					ArrayList<View> remainingViews= new ArrayList<View>();
					for(;position<list.size();position++){
						remainingViews.add((View) parent.getChildAt(position));
					}
					remainingViews.get(0).animate().setDuration(1000).y(v.getY());
					for(int i=1;i<remainingViews.size();i++){
						remainingViews.get(i).animate().setDuration(1000).y(remainingViews.get(i-1).getY());
					}
					v.animate().setDuration(1000).alpha(0)
					.withEndAction(new Runnable() {
						@Override
						public void run() {
//							String itemRemoving = null;
//							for (int i = 0; i < list.size(); i++) {
//								if(list.get(i).startsWith(item)){
//									itemRemoving = list.get(i);
//								}
//							}
							Log.d(TAG, item);
							list.remove(item);
							adapter.notifyDataSetChanged();
							onGoingTransition=false;
						}
					});
					boolean deleted =MySimpleArrayAdapter.deleteFile(item);
					Log.d(TAG, "Deleted? "+deleted);
				}
				return true;
			}
		});
	}
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		Bundle a= new Bundle();
		String item = (String) l.getItemAtPosition(position);
		Log.d(TAG, item);
		a.putString(WAITRESS_NAME,item);
		Intent waitress = new Intent(MenuTipCalc.this,TipCalc.class);
		waitress.putExtras(a);
		startActivity(waitress);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list_menu_calc, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.add_to_list:
			startActivityForResult(new Intent(this,AddToList.class), ADD_TO_LIST_REQUEST_CODE);
			return true;
		default:
			return false;
		}
	}
	
	protected void onStop(){
		Set<String> set = new HashSet<String>();
		set.addAll(list);
		SharedPreferences.Editor prefsEdit = prefs.edit();
		prefsEdit.putStringSet("array", set);
		prefsEdit.commit();
		super.onStop();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==ADD_TO_LIST_REQUEST_CODE){
			if(resultCode==RESULT_OK){
				Bundle bundle = data.getExtras();
				String name = bundle.getString("name");
				String place = bundle.getString("place");
				list.add(name+"-"+place);
				adapter.notifyDataSetChanged();
			}
		}
	}
} 


