package com.rafaelpol.tipcalculator;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class TipCalc extends Activity {

	private static final String TOTAL_Bill= "TOTAL_Bill";
	private static final String CURRENT_TIP= "CURRENTL_TIP";
	private static final String Bill_WITHOUT_TIP= "Bill_WITHOUT_TIP";
	private static final String NUMBER_OF_PEOPLE="NUMBER_OF_PEOPLE";
	private static final String TAG = "TipCalc";

	private double billBeforeTip;
	private int tipAmount;
	private double finalBill;

	private EditText billET,tipET;
	private TextView resultTV, tipSliderValue,finalTV,numberOfPeopleTV,waitressNameTV,placeTV;
	private SeekBar tipSeekBar;

	private CheckBox friendlyCheckBox,specialsCheckBox,opinionCheckBox;  

	private RadioGroup availabilityRadioGroup;
	private RadioButton goodRadioButton,badRadioButton,okRadioButton;

	private Button startButton,pauseButton,resetButton,plusButton,lessButton;

	private Spinner problemSpinner;

	private Chronometer timer;

	private int numberOfPeople=1;

	private String[] my_array={"Problem Solving","Good" , "Ok", "Bad"};

	private SharedPreferences prefs;
	
	private Bitmap waitressImage;
	
	private ImageView frame;

	private boolean timeOn=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE); //Remove TitleBar
		//Bundle gotBasket= getIntent().getExtras();
		//gotBread = gotBasket.getString("key");
		//question.setText(gotBread);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		prefs = getSharedPreferences("timer", MODE_PRIVATE);
		setContentView(R.layout.activity_tip_calc);
		Bundle fromList = getIntent().getExtras();
		if(savedInstanceState == null){  //Check if app was running
			billBeforeTip=0.0;
			tipAmount=15;
			finalBill=0.0;
		}
		else{	
			billBeforeTip=savedInstanceState.getDouble(Bill_WITHOUT_TIP); //Get saved variables
			tipAmount=savedInstanceState.getInt(CURRENT_TIP);
			finalBill=savedInstanceState.getDouble(TOTAL_Bill);
			numberOfPeople=savedInstanceState.getInt(NUMBER_OF_PEOPLE);
		}
		billET = (EditText) findViewById(R.id.billEditText);
		tipET = (EditText) findViewById(R.id.tipEditText1);

		resultTV = (TextView) findViewById(R.id.resultTextView01);
		finalTV = (TextView) findViewById(R.id.finalBillTextView01);
		tipSliderValue = (TextView) findViewById(R.id.tipSliderValueTv);
		numberOfPeopleTV = (TextView) findViewById(R.id.currentNumberOfPplTV);
		waitressNameTV = (TextView) findViewById(R.id.waitressNameTV);
		placeTV = (TextView) findViewById(R.id.placeTV);
		
		frame = (ImageView) findViewById(R.id.imageWaitress);
		String fromAdd;
		if(fromList!=null){
			fromAdd=fromList.getString(MenuTipCalc.WAITRESS_NAME);
			SharedPreferences.Editor preferencesEditor = prefs.edit();
			preferencesEditor.putString("waitressName", fromAdd);
			preferencesEditor.apply();
		}
		else{
			fromAdd = prefs.getString("waitressName", "Unicorn");
		}
		String nameAndPalce[] = fromAdd.split("-");
		waitressNameTV.setText(nameAndPalce[0]);
		placeTV.setText(nameAndPalce[1]);
		waitressImage = MySimpleArrayAdapter.retrieveBitmap(fromAdd);
		if(waitressImage==null){
			frame.setBackgroundResource(R.drawable.ic_launcher);
			waitressNameTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			placeTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
		}
		else{
			frame.setImageBitmap(waitressImage);
		}

		tipSeekBar= (SeekBar) findViewById(R.id.seekBar1);

		friendlyCheckBox = (CheckBox) findViewById(R.id.checkBox1);
		specialsCheckBox = (CheckBox) findViewById(R.id.checkBox2);
		opinionCheckBox = (CheckBox) findViewById(R.id.checkBox3);

		availabilityRadioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		goodRadioButton = (RadioButton) findViewById(R.id.radio0);
		badRadioButton = (RadioButton) findViewById(R.id.radio2);
		okRadioButton = (RadioButton) findViewById(R.id.radio1);

		problemSpinner = (Spinner) findViewById(R.id.problem_Spinner);
		ArrayAdapter<String> my_Adapter = new ArrayAdapter<String>(this, R.layout.spinner,my_array);
		problemSpinner.setAdapter(my_Adapter);

		startButton = (Button) findViewById(R.id.buttonStart);
		pauseButton = (Button) findViewById(R.id.buttonPause);
		resetButton = (Button) findViewById(R.id.resetbutton);
		plusButton = (Button) findViewById(R.id.plusButton);
		lessButton = (Button) findViewById(R.id.lessButton);

		timer =(Chronometer) findViewById(R.id.chronometer1);

		tipET.addTextChangedListener(tipChangedListener);    //Listeners
		billET.addTextChangedListener(billChangedListener);
		billET.setOnClickListener(clickedBill);
		tipSeekBar.setOnSeekBarChangeListener(tipSeekBarListener);
		setUpIntroCheckBoxes();
		addChangeListenerToRadios();
		addItemSelectedListenerToSpinner();
		setbuttonOnClickListeners();
		String timeSave = prefs.getString("time", null);
		if(timeSave!=null){
			timer.setText(prefs.getString("time", "00:00")); // set timer to save time
			Log.d(TAG, "Time took from last session "+timeSave);
		}
		if (prefs.getBoolean("timeOn", false)){  // get if timer  was running when onDestroy
			String array[] = timeSave.split(":");	 //get minutes and seconds in array
			int stoppedMilliseconds=0;
			if(array.length==3){
				stoppedMilliseconds = Integer.parseInt(array[0])*60*60*1000+Integer.parseInt(array[1])*60*1000+Integer.parseInt(array[2])*100; //convert it to milliseconds
			}
			else{
				stoppedMilliseconds = Integer.parseInt(array[0])*60*1000+Integer.parseInt(array[1])*1000; //convert it to milliseconds
			}
			long base=prefs.getLong("timeClockLastTime", SystemClock.elapsedRealtime())-stoppedMilliseconds;
			timer.setBase(base); 
			timer.start();		
			timeOn=true;// if on start timer
		}
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.d(TAG,"onStop()");
		SharedPreferences.Editor preferencesEditor = prefs.edit();
		if(!timeOn){
			preferencesEditor.putString("time", timer.getText().toString());
			preferencesEditor.apply();
			Log.d(TAG, "Time saved in prefs "+prefs.getString("time", null));
		}
		preferencesEditor.putBoolean("timeOn", timeOn);
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG,"onDestroy()");
		SharedPreferences.Editor preferencesEditor = prefs.edit();   //Save time
		preferencesEditor.putString("time", timer.getText().toString());
		preferencesEditor.putBoolean("timeOn", timeOn);
		preferencesEditor.putLong("timeClockLastTime", SystemClock.elapsedRealtime());
		preferencesEditor.apply();
		Log.d(TAG, "Time saved in prefs "+prefs.getString("time", null));
		super.onDestroy();
	}
	private void setbuttonOnClickListeners() {
		// TODO Auto-generated method stub
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!timeOn){
					int stoppedMilliseconds=0;
					if(prefs.getString("time", "00:00").equals("00:00") || !prefs.getString("time", "00:00").equals(timer.getText().toString())){
						String chronoText= timer.getText().toString(); //get time from timer
						String array[] = chronoText.split(":");	 //get minutes and seconds in array
						if(array.length==3){
							stoppedMilliseconds = Integer.parseInt(array[0])*60*60*1000+Integer.parseInt(array[1])*60*1000+Integer.parseInt(array[2])*100; //convert it to milliseconds
						}
						else{
							stoppedMilliseconds = Integer.parseInt(array[0])*60*1000+Integer.parseInt(array[1])*1000; //convert it to milliseconds
						}
						Log.d(TAG, "Timer started normally "+chronoText);
					}
					else{
						String chronoText= prefs.getString("time", "00:00");
						String array[] = chronoText.split(":");	 //get minutes and seconds in array
						if(array.length==3){
							stoppedMilliseconds = Integer.parseInt(array[0])*60*60*1000+Integer.parseInt(array[1])*60*1000+Integer.parseInt(array[2])*100; //convert it to milliseconds
						}
						else{
							stoppedMilliseconds = Integer.parseInt(array[0])*60*1000+Integer.parseInt(array[1])*1000; //convert it to milliseconds
						}
						Log.d(TAG, "Timer started from saved time " +chronoText);
					}
					timer.setBase(SystemClock.elapsedRealtime()-stoppedMilliseconds); 
					timeOn=true;
					timer.start();
				}
			}
		});

		pauseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				timer.stop();
				if(timeOn){
					SharedPreferences.Editor preferencesEditor = prefs.edit();
					preferencesEditor.putString("time", timer.getText().toString());
					preferencesEditor.apply();
					Log.d(TAG, "Time saved in prefs "+prefs.getString("time", null));
				}
				timeOn=false;
			}

		});

		resetButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				timer.stop();
				timer.setBase(SystemClock.elapsedRealtime());
				timeOn=false;
			}

		});
		plusButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//peopleET.setText(Integer.toString(Integer.parseInt(peopleET.getText().toString()) +1));
				numberOfPeople++;
				numberOfPeopleTV.setText(Integer.toString(numberOfPeople));
				updateTipAndFinalBill();
			}
		});
		lessButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(numberOfPeople >1){
					//peopleET.setText(Integer.toString(Integer.parseInt(peopleET.getText().toString())-1));
					numberOfPeople--;
					numberOfPeopleTV.setText(Integer.toString(numberOfPeople));
					updateTipAndFinalBill();
				}
			}
		});
	}
	private void addItemSelectedListenerToSpinner() {
		// TODO Auto-generated method stub
		problemSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				Object selected=problemSpinner.getSelectedItem();
				if(selected.equals("Bad")) tipAmount-=2;
				else if(selected.equals("Good")) tipAmount+=2;
				else if(selected.equals("Ok")) tipAmount+=1;
				setTipFromWaitressCheckList();
				updateTipAndFinalBill();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
	}
	private void addChangeListenerToRadios() {
		// TODO Auto-generated method stub
		availabilityRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(checkedId==goodRadioButton.getId()){
					tipAmount+=2;

				}
				else if(checkedId==okRadioButton.getId()){
					tipAmount+=1;

				}
				else if(checkedId==badRadioButton.getId()){
					tipAmount-=2;

				}
				setTipFromWaitressCheckList();
				updateTipAndFinalBill();
			}
		});
	}
	private void setUpIntroCheckBoxes() {
		// TODO Auto-generated method stub
		friendlyCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				tipAmount+=isChecked?2:-2;
				setTipFromWaitressCheckList();
				updateTipAndFinalBill();
			}
		});

		specialsCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				tipAmount+=isChecked?1:-1;
				setTipFromWaitressCheckList();
				updateTipAndFinalBill();
			}
		});

		opinionCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				tipAmount+=isChecked?1:-1;
				setTipFromWaitressCheckList();
				updateTipAndFinalBill();
			}
		});
	}
	private void setTipFromWaitressCheckList() {
		// TODO Auto-generated method stub
		tipET.setText(Integer.toString(tipAmount));
		tipSliderValue.setText(Integer.toString(tipAmount));
	}
	private OnSeekBarChangeListener tipSeekBarListener = new OnSeekBarChangeListener() {  

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			tipET.setText(Integer.toString(tipAmount));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			tipET.setText(Integer.toString(tipAmount));
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			tipAmount = tipSeekBar.getProgress();
			tipSliderValue.setText(Integer.toString(tipSeekBar.getProgress()));
			updateTipAndFinalBill();
		}
	};
	private OnClickListener clickedBill = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			billET.setText("");
		}
	};
	private TextWatcher billChangedListener = new TextWatcher(){

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

			try{
				billBeforeTip=Double.parseDouble(s.toString());
			}
			catch(NumberFormatException e){
				billBeforeTip=0.0;

			}
			updateTipAndFinalBill();
		}

	};
	private TextWatcher tipChangedListener = new TextWatcher(){

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

			try{
				tipAmount=Integer.parseInt(s.toString());
			}
			catch(NumberFormatException e){
				tipAmount=0;
			}
			if(tipAmount>100){
				tipAmount=100;
				tipET.setText(Integer.toString(tipAmount));
			}
			else if(tipAmount<0){
				tipAmount=0;
				tipET.setText(Integer.toString(tipAmount));
			}
			tipSliderValue.setText(Integer.toString(tipAmount));
			tipSeekBar.setProgress(Integer.parseInt(tipSliderValue.getText().toString()));
			updateTipAndFinalBill();
		}

	};

	private void updateTipAndFinalBill() {
		// TODO Auto-generated method stub
		if(numberOfPeople>1){
			finalBill = (billBeforeTip + (billBeforeTip*tipAmount*.01));
			double perPerson=finalBill/numberOfPeople;
			finalTV.setText("Final Bill = "+String.format("%.02f", finalBill));
			resultTV.setText("Per person = "+String.format("%.02f", perPerson));
		}
		else{
			finalBill = billBeforeTip + (billBeforeTip*tipAmount*.01);
			finalTV.setText("Final Bill");
			resultTV.setText(String.format("%.02f", finalBill));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putDouble(Bill_WITHOUT_TIP, billBeforeTip);
		outState.putInt(CURRENT_TIP, tipAmount);
		outState.putDouble(TOTAL_Bill, finalBill);
		outState.putInt(NUMBER_OF_PEOPLE, numberOfPeople);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.tip_calc, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.back_to_list_option:
			startActivity(new Intent(this,MenuTipCalc.class));
			return true;
		default:
			return false;
		}
	}
}
