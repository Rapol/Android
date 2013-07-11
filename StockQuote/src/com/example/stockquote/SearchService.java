package com.example.stockquote;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class SearchService extends IntentService{
	static final String TAG="SearchService";
	static final String COMPAY_NAME="com.example.searchservice.COMPANY_NAME";
	static final String CONNECTION_INFO="com.example.searchservice.CONNECTION_INFO";
	private String result="";
	private boolean connection=true;
	public SearchService() {
		super(TAG);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		String stock = intent.getStringExtra(StockActivity.STOCK_SYMBOL);
		ResultReceiver rec = intent.getParcelableExtra("receiver");
		String stockUrl = StockInfoActivity.yahooURLFirst+stock+StockInfoActivity.yahooURLSecond;
		try{
			URL url =new URL(stockUrl);
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
						result= getTextValue(docEle, "Name");
						//Checks if company exists if it doesnt exist nullpointerexception gets called
						getTextValue(docEle, "YearLow");
					}
				}
			}
		}catch(NullPointerException e){
			e.printStackTrace();
			result="";
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			connection=false;
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			connection=false;
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			connection=false;
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			connection=false;
			e.printStackTrace();
		}
		Bundle sendBack = new Bundle();
		sendBack.putString(COMPAY_NAME, result);
		sendBack.putBoolean(CONNECTION_INFO, connection);
		Log.d(TAG, result);
		rec.send(0, sendBack);
	}
	
	private String getTextValue(Element entry, String tagName){

		String tagValueToReturn = null;

		NodeList nl = entry.getElementsByTagName(tagName);

		if(nl != null && nl.getLength() > 0){

			Element element = (Element) nl.item(0);

			tagValueToReturn = element.getFirstChild().getNodeValue();
		}

		return tagValueToReturn;
	}
}
