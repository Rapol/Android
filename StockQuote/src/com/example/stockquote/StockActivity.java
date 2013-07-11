package com.example.stockquote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData.Item;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.example.stockquote.MyResultReceiver.Receiver;

public class StockActivity extends Activity implements Receiver{

	public final static String STOCK_SYMBOL ="com.example.stockquote.STOCK";
	public final static String STOCK_COMPANY ="com.example.stockquote.COMPANY";
	public final static String TAG ="StockActivity";
	private SharedPreferences stockSymbolEntered;
	private TableLayout stockTableScrollView;
	private EditText stockSymbolET;
	private Button enterStockSymbolButton,deleteStockButton;
	public MyResultReceiver resultReciever;
	private String companyName;
	private ProgressDialog mDialog;
	private ArrayList<View> allTableRow;
	private ArrayList<String> selectedCompanies;
	private MenuItem deleteItem;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		stockSymbolEntered =getSharedPreferences("stockList", MODE_PRIVATE);
		stockTableScrollView = (TableLayout) findViewById(R.id.StockScrollView);
		stockSymbolET = (EditText) findViewById(R.id.stockSymboET);
		enterStockSymbolButton = (Button) findViewById(R.id.entetStockSymbolButton);
		deleteStockButton = (Button) findViewById(R.id.deleteSymbolsButton);
		enterStockSymbolButton.setOnClickListener(enterStockSymbolButtonListener);
		deleteStockButton.setOnClickListener(deleteStockButtonListener);
		allTableRow=new ArrayList<View>(10);
		selectedCompanies = new ArrayList<String>(10);
		
		updateSavedStockList(null);

		resultReciever = new MyResultReceiver(new Handler());

		resultReciever.setReceiver(this);

		stockSymbolET.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
					enterStockSymbolButton.performClick();
					Log.i(TAG,"Enter pressed");
				}    
				return false;
			}
		});
	}

	private void updateSavedStockList(String newStockSymbol) {
		// TODO Auto-generated method stub
		String[] stocks = stockSymbolEntered.getAll().keySet().toArray(new String [0]);
		Arrays.sort(stocks,String.CASE_INSENSITIVE_ORDER);
		for(String s: stocks){
			Log.d(TAG, s);
		}
		if(newStockSymbol !=null){
			//add new stock symbol to list
			insertStockInScrollView(newStockSymbol,Arrays.binarySearch(stocks, newStockSymbol));
		}
		else{
			//populate stocks list with saved list
			for(int i=0; i<stocks.length;i++){
				insertStockInScrollView(stocks[i],i);
			}
		}
	}

	private void saveStockSymbol(String newStock){
		// Used to check if this is a new stock
		String isTheStockNew = stockSymbolEntered.getString(newStock, null);
		// If this is a new stock add its components
		if(isTheStockNew == null){
			// Editor is used to store a key / value pair
			// I'm using the stock symbol for both, but I could have used company
			// name or something else
			SharedPreferences.Editor preferencesEditor = stockSymbolEntered.edit();
			preferencesEditor.putString(newStock, newStock);
			preferencesEditor.apply();
			//Add it to the list
			updateSavedStockList(newStock);
		}
	}
	private void deleteStockSelected(){
		SharedPreferences.Editor preferencesEditor = stockSymbolEntered.edit();
		for (String s : selectedCompanies) {
			preferencesEditor.remove(s);
		}
		preferencesEditor.apply();
		deleteAllStocks();
		updateSavedStockList(null);
		selectedCompanies.clear();
	}
	private void insertStockInScrollView(String stock, int arrayIndex){

		// Get the LayoutInflator service
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Use the inflater to inflate a stock row from stock_quote_row.xml
		View newStockRow = inflater.inflate(R.layout.stock_quote_row, null);


		// Create the TextView for the ScrollView Row
		TextView newStockTextView = (TextView) newStockRow.findViewById(R.id.dynamicStockSymbolTV);

		TextView stockCompanyName = (TextView) newStockRow.findViewById(R.id.dynamicStockCompanyTV);

		String info[]=stock.split("/");
		// Add the stock symbol to the TextView
		newStockTextView.setText(info[0]);

		stockCompanyName.setText(info[1]);

		Button stockQuoteButton = (Button) newStockRow.findViewById(R.id.stockQuoteButton);
		stockQuoteButton.setOnClickListener(getStockActivityListener);

		Button quoteFromWebButton = (Button) newStockRow.findViewById(R.id.quoteStockFromWebButton);
		quoteFromWebButton.setOnClickListener(getStockFromWebsiteListener);

		// Add the new components for the stock to the TableLayout
		stockTableScrollView.addView(newStockRow, arrayIndex);
		allTableRow.add(newStockRow);
		newStockRow.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				TableRow tableClicked = (TableRow) allTableRow.get(allTableRow.indexOf(v)); // get tableRow Clicked
				TextView stockSymbol = (TextView) tableClicked.getChildAt(0); // Get both textview of tableRow
				TextView companyName = (TextView) tableClicked.getChildAt(1);
				String formatted = stockSymbol.getText()+"/"+companyName.getText(); //Format the two textviews
				int selected = selectedCompanies.indexOf(formatted); // get index of the selected company in the array selectedCompanies
				if(selected>=0){ 									//esta en la lista..remover
					v.setBackgroundColor(Color.TRANSPARENT);		
					selectedCompanies.remove(selected);
					if(selectedCompanies.size()==0){
						deleteItem.setVisible(false);
					}
				}
				else{
					v.setBackgroundColor(Color.RED);
					selectedCompanies.add(formatted);
					deleteItem.setVisible(true);
				}
				for(String s: selectedCompanies){
					Log.d(TAG, s);
				}
				//				for (int i = 0; i < allTableRow.size(); i++) {
				//					if(v.equals(allTableRow.get(i))){
				//						tableClicked=(TableRow) allTableRow.get(i);
				//					}
				//				}
				//				TextView companyCliked = (TextView) tableClicked.getChildAt(0);
				//				Log.d(TAG, companyCliked.getText().toString());
				//				if(v instanceof TableRow){
				//				TableLayout a = (TableLayout) v.getParent();
				//				TableRow b =(TableRow) a.getChildAt(0);
				//				Log.d(TAG, ""+a.equals(stockTableScrollView));
				//				TextView c = (TextView) b.getChildAt(0);
				//				Log.d(TAG, c.getText().toString());
				//				}
				return false;
			}
		});
	}

	public OnClickListener enterStockSymbolButtonListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(stockSymbolET.getText().length()>0){
				mDialog = new ProgressDialog(StockActivity.this);
				mDialog.setMessage("Please wait...");
				mDialog.show();
				Intent service = new Intent(StockActivity.this,SearchService.class);
				service.putExtra("receiver", resultReciever);
				service.putExtra(STOCK_SYMBOL, stockSymbolET.getText().toString().toUpperCase(Locale.getDefault()));
				startService(service);
				InputMethodManager imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(stockSymbolET.getWindowToken(), 0);
			}
			else{
				AlertDialog.Builder builder = new AlertDialog.Builder(StockActivity.this);
				builder.setTitle(R.string.invalid_stock_symbol);
				builder.setPositiveButton(R.string.ok, null);
				builder.setMessage(R.string.missing_stock_symbol);
				AlertDialog theAlertDialog = builder.create();
				theAlertDialog.show();
			}
		}

	};
	private void deleteAllStocks(){
		stockTableScrollView.removeAllViews();
	}

	public OnClickListener deleteStockButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			deleteAllStocks();
			SharedPreferences.Editor preferences = stockSymbolEntered.edit();
			preferences.clear();
			preferences.apply();
			selectedCompanies.clear();
			deleteItem.setVisible(false);
		}
	};
	public OnClickListener getStockActivityListener =new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			TableRow tableRow= (TableRow) v.getParent();
			TextView stockTV= (TextView) tableRow.findViewById(R.id.dynamicStockSymbolTV);
			TextView stockCompanyNameTV= (TextView) tableRow.findViewById(R.id.dynamicStockCompanyTV);
			String stockSymbol= stockTV.getText().toString();
			String stockName = stockCompanyNameTV.getText().toString();
			Intent intent = new Intent(StockActivity.this, StockInfoActivity.class);
			intent.putExtra(STOCK_SYMBOL, stockSymbol);
			intent.putExtra(STOCK_COMPANY, stockName);
			startActivity(intent);
		}
	};
	public OnClickListener getStockFromWebsiteListener =new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			TableRow tableRow= (TableRow) v.getParent();
			TextView stockTV= (TextView) tableRow.findViewById(R.id.dynamicStockSymbolTV);
			String stockSymbol= stockTV.getText().toString();
			String stockURL=getString(R.string.yahoo_stock_url)+stockSymbol;
			Intent getStockWebPage = new Intent(Intent.ACTION_VIEW,Uri.parse(stockURL));
			startActivity(getStockWebPage);
		}
	};


	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_ENTER:
			enterStockSymbolButton.performClick();
			return true;
		default:
			return super.onKeyUp(keyCode, event);
		}
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		// TODO Auto-generated method stub
		companyName = resultData.getString(SearchService.COMPAY_NAME);
		if(companyName != ""){
			saveStockSymbol(stockSymbolET.getText().toString().toUpperCase(Locale.getDefault())+"/"+companyName);
		}
		else{
			if(!resultData.getBoolean(SearchService.CONNECTION_INFO)){
				AlertDialog.Builder builder = new AlertDialog.Builder(StockActivity.this);
				builder.setTitle(R.string.failed_search);
				builder.setPositiveButton(R.string.ok, null);
				builder.setMessage(R.string.error_search);
				AlertDialog theAlertDialog = builder.create();
				theAlertDialog.show();
			}
			else{
				AlertDialog.Builder builder = new AlertDialog.Builder(StockActivity.this);
				builder.setTitle(R.string.invalid_stock_symbol);
				builder.setPositiveButton(R.string.ok, null);
				builder.setMessage(R.string.no_stock_symbol);
				AlertDialog theAlertDialog = builder.create();
				theAlertDialog.show();
			}
		}
		stockSymbolET.setText("");
		mDialog.dismiss();
	}

	//	final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
	//	    public void onLongPress(MotionEvent e) {
	//	        Log.d(TAG, "Longpress detected");
	//	    }
	//	});
	//
	//	public boolean onTouchEvent(MotionEvent event) {
	//	    return gestureDetector.onTouchEvent(event);
	//	};
	//	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stock, menu);
		deleteItem = menu.findItem(R.id.menu_delete);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.action_settings:
			startActivity(new Intent(StockActivity.this,SettingsActivity.class));
			Log.d(TAG, "hello?");
			return true;
		case R.id.menu_delete:
			deleteStockSelected();
			deleteItem.setVisible(false);
		default:
			return false;
		}
	}
}

