package com.rafaelpol.tipcalculator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class AddToList extends Activity{

	EditText nameET,placeET;
	Button enterButton,captureButton;
	ImageView imgView;
	final static int IMAGE_REQUEST_CODE=1;
	final static String TAG="ADDTOLIST";
	Bitmap bitmap;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addtolist);
		nameET = (EditText) findViewById(R.id.nameET);
		placeET = (EditText) findViewById(R.id.placeET);
		enterButton = (Button) findViewById(R.id.enterButton);
		imgView = (ImageView) findViewById(R.id.imageView);
		captureButton = (Button) findViewById(R.id.captureButton);
		captureButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String name = nameET.getText().toString() + placeET.getText().toString();
				if(!name.equals("")){
					Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(i, IMAGE_REQUEST_CODE);
				}
				else{
					Toast.makeText(getApplicationContext(), "Please enter name and place", Toast.LENGTH_LONG).show();
				}
			}
		});
		enterButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String name = nameET.getText().toString();
				String place = placeET.getText().toString();
				if(!name.equals("") && !place.equals("")){
					Intent i = new Intent();
					Bundle bundle= new Bundle();
					bundle.putString("name", name);
					bundle.putString("place", place);
					i.putExtras(bundle);
					setResult(RESULT_OK, i);
					if(bitmap!=null){
						saveBitmap(bitmap);
					}
					finish();
				}
				else{
					Toast.makeText(getApplicationContext(), "Please enter name and place", Toast.LENGTH_LONG).show();
				}
			}
		});

	}
	public void saveBitmap(Bitmap bmp)
	{
		String name = nameET.getText().toString();
		String place = placeET.getText().toString();
		String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + 
				"/TipCalc";
		File dir = new File(file_path);
		if(!dir.exists())
			dir.mkdirs();
		File file = new File(dir, name+"-"+place+".png");
		Log.d(TAG, name+"-"+place+".png");
		try {
			FileOutputStream fOut = new FileOutputStream(file);
			bmp.compress(Bitmap.CompressFormat.PNG, 85, fOut);
			fOut.flush();
			fOut.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==IMAGE_REQUEST_CODE){
			if(resultCode==RESULT_OK){
				Bundle bundle = data.getExtras();
				bitmap = (Bitmap) bundle.get("data");
				imgView.setImageBitmap(bitmap);
			}
		}
	}

}
