package com.rafaelpol.tipcalculator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MySimpleArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final List<String> values;

	public MySimpleArrayAdapter(Context context, List<String> values) {
		super(context, R.layout.rowlawyout, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.rowlawyout, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		TextView placeTV = (TextView) rowView.findViewById(R.id.labelPlace);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		textView.setText(values.get(position).split("-")[0]);
		placeTV.setText(values.get(position).split("-")[1]);
		// Change the icon for Windows and iPhone
		String s = values.get(position);
		Bitmap picture= retrieveBitmap(s);
		if(picture==null){
			imageView.setImageResource(R.drawable.ic_launcher);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			placeTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
		}
		else{
			imageView.setImageBitmap(picture);
		}
//		if (s.startsWith("Windows7") || s.startsWith("iPhone")
//				|| s.startsWith("Solaris")) {
//			imageView.setImageResource(R.drawable.ic_launcher);
//		} else {
//			imageView.setImageResource(R.drawable.ic_launcher1);
//		}

		return rowView;
	}
	public static Bitmap retrieveBitmap(String s){
		Bitmap bmp;
		String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + 
				"/TipCalc";
		File dir = new File(file_path);
		if(!dir.exists())
			return null;
		File file = new File(dir, s+".png");
		try {
			FileInputStream fin = new FileInputStream(file);
			bmp = BitmapFactory.decodeStream(fin);
			fin.close();
			return bmp;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static boolean deleteFile(String s){
		String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +"/TipCalc/"+s+".png";
		File file = new File(file_path);
		return file.delete();
	}
} 