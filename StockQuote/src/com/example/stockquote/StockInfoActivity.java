package com.example.stockquote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class StockInfoActivity extends Activity{
	private static final String TAG="STOCKQUOTE";

	// Define the TextViews I use in activity_stock_info.xml

	TextView companyNameTextView;
	TextView yearLowTextView;
	TextView yearHighTextView;
	TextView daysLowTextView;
	TextView daysHighTextView;
	TextView lastTradePriceOnlyTextView;
	TextView changeTextView;
	TextView daysRangeTextView;

	ImageView imageView;

	String companyName;

	// XML node keys
	static final String KEY_ITEM = "quote"; // parent node
	static final String KEY_NAME = "Name";
	static final String KEY_YEAR_LOW = "YearLow";
	static final String KEY_YEAR_HIGH = "YearHigh";
	static final String KEY_DAYS_LOW = "DaysLow";
	static final String KEY_DAYS_HIGH = "DaysHigh";
	static final String KEY_LAST_TRADE_PRICE = "LastTradePriceOnly";
	static final String KEY_CHANGE = "Change";
	static final String KEY_DAYS_RANGE = "DaysRange";

	// XML Data to Retrieve
	String name = "";
	String yearLow = "";
	String yearHigh = "";
	String daysLow = "";
	String daysHigh = "";
	String lastTradePriceOnly = "";
	String change = "";
	String daysRange = "";

	String stockSymbol;
	boolean connection=true;

	// Used to make the URL to call for XML data
	static final String yahooURLFirst = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quote%20where%20symbol%20in%20(%22";
	static final String yahooURLSecond = "%22)&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_stock_info);
		Intent intent= getIntent();
		stockSymbol =intent.getStringExtra(StockActivity.STOCK_SYMBOL);
		companyName = intent.getStringExtra(StockActivity.STOCK_COMPANY);
		// Initialize TextViews
		companyNameTextView = (TextView) findViewById(R.id.companyNameTextView);
		yearLowTextView = (TextView) findViewById(R.id.yearLowTextView);
		yearHighTextView = (TextView) findViewById(R.id.yearHighTextView);
		daysLowTextView = (TextView) findViewById(R.id.daysLowTextView);
		daysHighTextView = (TextView) findViewById(R.id.daysHighTextView);
		lastTradePriceOnlyTextView = (TextView) findViewById(R.id.lastTradePriceOnlyTextView);
		changeTextView = (TextView) findViewById(R.id.changeTextView);
		daysRangeTextView = (TextView) findViewById(R.id.daysRangeTextView);
		imageView = (ImageView) findViewById(R.id.imageView);
		String companyNameFormated = companyName.replace(" ","%20");
		Log.d(TAG, companyNameFormated);

		// Sends a message to the LogCat
		Log.d(TAG, "Before URL Creation " + stockSymbol);

		// Create the YQL query
		final String yqlURL = yahooURLFirst + stockSymbol + yahooURLSecond;

		new MyAsyncTask().execute(yqlURL);
		if(((StockApp)getApplication()).picture){
			ImgDownload i = new ImgDownload(companyNameFormated);
			i.execute();
		}
	}

	private class MyAsyncTask extends AsyncTask<String,String,String>{

		private ProgressDialog mDialog;
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			if(!((StockApp)getApplication()).picture){
				mDialog = new ProgressDialog(StockInfoActivity.this);
				mDialog.setMessage("Please wait...");
				mDialog.show();
			}
		}
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try{
				URL url =new URL(params[0]);
				URLConnection connection= url.openConnection();
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				int responceCode = httpConnection.getResponseCode();
				if(responceCode == HttpURLConnection.HTTP_OK){
					InputStream in = httpConnection.getInputStream();
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db=dbf.newDocumentBuilder();
					Document dom = db.parse(in);
					Element docEle =dom.getDocumentElement();
					NodeList nl =docEle.getElementsByTagName("quote");
					if (nl != null && nl.getLength() > 0) {

						// Cycles through if we find multiple quote tags
						// Mainly used for demonstration purposes
						for (int i = 0 ; i < nl.getLength(); i++) {

							// Passes the root element of the XML page, so 
							// that the function below can search for the 
							// information needed
							StockInfo theStock = getStockInformation(docEle);

							// Gets the values stored in the StockInfo object
							daysLow = theStock.getDaysLow();
							daysHigh = theStock.getDaysHigh();
							yearLow = theStock.getYearLow();
							yearHigh = theStock.getYearHigh();
							name = theStock.getName();
							lastTradePriceOnly = theStock.getLastTradePriceOnly();
							change = theStock.getChange();
							daysRange = theStock.getDaysRange();

							// Outputs information for tracking reasons only
							Log.d(TAG, "Stock Name " + name);
							Log.d(TAG, "Stock Year High " + yearHigh);
							Log.d(TAG, "Stock Year Low " + yearLow);
							Log.d(TAG, "Stock Days High " + daysHigh);
							Log.d(TAG, "Stock Days Low " + daysLow);
						}
					}
					else{
						Log.d(TAG, "NL is null");
					}
				}
				else{
					Log.d(TAG, "Http connection not ok");
				}
			} catch (MalformedURLException e) {
				connection=false;
				Log.d(TAG, "MalformedURLException", e);
			} catch (IOException e) {
				connection=false;
				Log.d(TAG, "IOException", e);
			} catch (ParserConfigurationException e) {
				connection=false;
				Log.d(TAG, "Parser Configuration Exception", e);
			} catch (SAXException e) {
				connection=false;
				Log.d(TAG, "SAX Exception", e);
			}catch (NullPointerException e) {
				Log.d(TAG, "NullPointerException; company "+stockSymbol+" does not exist or yahoo service unavailable", e);
			}
			return null;
		}

		// Changes the values for a bunch of TextViews on the GUI
		protected void onPostExecute(String result){
			if(!((StockApp)getApplication()).picture){
				mDialog.dismiss();
			}
			if(connection){
				companyNameTextView.setText(name);
				yearLowTextView.setText("Year Low: " + yearLow);
				yearHighTextView.setText("Year High: " + yearHigh);
				daysLowTextView.setText("Days Low: " + daysLow);
				daysHighTextView.setText("Days High: " + daysHigh);
				lastTradePriceOnlyTextView.setText("Last Price: " + lastTradePriceOnly);
				changeTextView.setText("Change: " + change);
				daysRangeTextView.setText("Daily Price Range: " + daysRange);
			}
			else{
				AlertDialog.Builder builder = new AlertDialog.Builder(StockInfoActivity.this);
				builder.setTitle(R.string.failed_search);
				builder.setPositiveButton(R.string.ok, null);
				builder.setMessage(R.string.error_search);
				AlertDialog theAlertDialog = builder.create();
				theAlertDialog.show();
			}

		}


	}

	// Sends the root xml tag and the tag name we are searching for to
	// getTextValue for processing. Then uses that information to create
	// a new StockInfo object
	private StockInfo getStockInformation(Element entry){
		String stockName = getTextValue(entry, "Name");
		String stockYearLow = getTextValue(entry, "YearLow");
		String stockYearHigh = getTextValue(entry, "YearHigh");
		String stockDaysLow = getTextValue(entry, "DaysLow");
		String stockDaysHigh = getTextValue(entry, "DaysHigh");
		String stocklastTradePriceOnlyTextView = getTextValue(entry, "LastTradePriceOnly");
		String stockChange = getTextValue(entry, "Change");
		String stockDaysRange = getTextValue(entry, "DaysRange");

		StockInfo theStock = new StockInfo(stockDaysLow, stockDaysHigh, stockYearLow,
				stockYearHigh, stockName, stocklastTradePriceOnlyTextView,
				stockChange, stockDaysRange);

		return theStock;

	}

	// Searches through the XML document for a tag that matches 
	// the tagName passed in. Then it gets the value from that
	// tag and returns it

	private String getTextValue(Element entry, String tagName){

		String tagValueToReturn = null;

		NodeList nl = entry.getElementsByTagName(tagName);

		if(nl != null && nl.getLength() > 0){

			Element element = (Element) nl.item(0);

			tagValueToReturn = element.getFirstChild().getNodeValue();
		}

		return tagValueToReturn;
	}
	private class ImgDownload extends AsyncTask<Object, Object, Object> {
		private Bitmap pic;
		private String name;
		private ProgressDialog mDialog;

		public ImgDownload(String company){
			name=company;
		}
		@Override
		protected void onPreExecute(){
			super.onPreExecute();

			mDialog = new ProgressDialog(StockInfoActivity.this);
			mDialog.setMessage("Please wait...");
			mDialog.show();
		}
		@Override
		protected Object doInBackground(Object... objects) {
			URL url;
			try {
				if(((StockApp)getApplication()).pictureSize.equals("Standard")){
					url = new URL("https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q="+name+"%20logo");
				}
				else{
					url = new URL("https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q="+name+"%20logo&imgsz="+((StockApp)getApplication()).pictureSize.toLowerCase());
				}
				URLConnection connection = url.openConnection();
				String line;
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while((line = reader.readLine()) != null) {
					builder.append(line);
				}
				JSONObject json = new JSONObject(builder.toString());
				JSONObject responseData =json.getJSONObject("responseData");
				JSONArray results = responseData.getJSONArray("results");
				String jsonURL=results.getJSONObject(0).getString("url");
				Log.d(TAG, jsonURL);
//				for(int i=0;i<results.length();i++){
//					JSONObject  a= results.getJSONObject(i);
//					int width = Integer.parseInt(a.getString("width"));
//					int height = Integer.parseInt(a.getString("height"));
//					Log.d(TAG, a.getString("url"));
//					if(width>200 && width<700 && height>200 && height<700){
//						jsonURL=a.getString("url");
//						break;
//					}
//				}
				Log.d(TAG, jsonURL);
				URL url2 = new URL(jsonURL);
				URLConnection conn = url2.openConnection();
				pic = BitmapFactory.decodeStream(conn.getInputStream());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object o) {
			imageView.setImageBitmap(pic);
			mDialog.dismiss();
		}
	}
}

