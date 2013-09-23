package com.zju.autosmsapp;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

public class SetSMS extends Activity{
	Calendar calendar = Calendar.getInstance(); 
	private Button save_btn;
	private Button delete_btn;
	private ImageButton contact_btn;
	private EditText phone;
	private EditText message;
	private TimePicker timePicker;
	private DatePicker datePicker;
	private int mId;
	private SMS sms;
	private int REQUEST_CONTACT = 1; 
	private boolean isEdit = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_sms);
		save_btn = (Button)findViewById(R.id.sms_save);
		delete_btn = (Button)findViewById(R.id.sms_delete);
		contact_btn = (ImageButton)findViewById(R.id.contact);
		phone = (EditText)findViewById(R.id.phnum);
		message = (EditText)findViewById(R.id.message);
		timePicker = (TimePicker)findViewById(R.id.timePicker);
		datePicker = (DatePicker)findViewById(R.id.datePicker);
		
		Intent i = getIntent();
        mId = i.getIntExtra(SMSMain.SMS_ID, -1);
        if (true) {
            Log.v("zju", "In SetSMS, sms id = " + mId);
        }
        
        //SMS sms;
        if(mId == -1){
        	sms = new SMS();
        }else{//edit a sms
        	isEdit = true;
        	sms = SMSMain.getSMS(getContentResolver(), mId);
        	if(sms == null){
        		finish();
        		return;
        	}else{
        		phone.setText(sms.phone);
        		message.setText(sms.message);
        		timePicker.setCurrentHour(sms.hour);
        		timePicker.setCurrentMinute(sms.minutes);
        		datePicker.init(sms.year, sms.month, sms.date, null);
        	}
        }
		
		
		save_btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(isEdit){//����������������������������������������������update
					updateSMS(sms);
				}
				else{//����������������������������������������������
					addSMS(sms);
				}
			}
		});
		
		delete_btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
			
		});
		
		contact_btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_PICK);
				intent.setData(ContactsContract.Contacts.CONTENT_URI);
				//Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, REQUEST_CONTACT);
			}
		});
	}

	protected void addSMS(SMS sms) {
		sms.phone = phone.getText().toString();
		sms.message = message.getText().toString();
		sms.year = datePicker.getYear();
		sms.month = datePicker.getMonth();
		sms.date = datePicker.getDayOfMonth();
		sms.hour = timePicker.getCurrentHour();
		sms.minutes = timePicker.getCurrentMinute();
		if(sms.phone.isEmpty()||sms.message.isEmpty()){
			Toast.makeText(getApplicationContext(), "������������������������",
				     Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(isTimeLegal(sms.year,sms.month,sms.date,sms.hour,sms.minutes)){
			sms.sendTime = SMSMain.calculateTime(sms);
			//check if the send-time is after the current time
			Log.i("setSMS.addSMS", sms.phone + "/" + sms.message + "/" + sms.year + "/" + sms.month + "/" + sms.date + "/" + sms.hour + "/" + sms.minutes + "/" + sms.sendTime);
			SMSMain.addSMS(this, sms);
			finish();
		}else{
			Toast.makeText(getApplicationContext(), "������������������������������",
				     Toast.LENGTH_SHORT).show();
		}
	}
	
	protected void updateSMS(SMS sms) {
		sms.phone = phone.getText().toString();
		sms.message = message.getText().toString();
		sms.year = datePicker.getYear();
		sms.month = datePicker.getMonth();
		sms.date = datePicker.getDayOfMonth();
		sms.hour = timePicker.getCurrentHour();
		sms.minutes = timePicker.getCurrentMinute();
		sms.sendTime = SMSMain.calculateTime(sms);
		if(isTimeLegal(sms.year,sms.month,sms.date,sms.hour,sms.minutes)){
			sms.sendTime = SMSMain.calculateTime(sms);
			//check if the send-time is after the current time
			Log.i("setSMS.addSMS", sms.phone + "/" + sms.message + "/" + sms.year + "/" + sms.month + "/" + sms.date + "/" + sms.hour + "/" + sms.minutes + "/" + sms.sendTime);
			SMSMain.updateSMS(this, sms);
			finish();
		}else{
			Toast.makeText(getApplicationContext(), "������������������������������",
				     Toast.LENGTH_SHORT).show();
		}
	}
	
	protected void deleteSMS(SMS sms){
		SMSMain.deleteSMS(SetSMS.this, sms.id);
        finish();
	}
	
	protected boolean isTimeLegal(int year,int month,int date, int hour,int minute){
		boolean isLegal = false;
		Calendar c = Calendar.getInstance();
		int curYear = c.get(Calendar.YEAR);
		int curMonth = c.get(Calendar.MONTH);
		int curDay = c.get(Calendar.DAY_OF_MONTH);
		int curHour = c.get(Calendar.HOUR_OF_DAY);
		int curMinute = c.get(Calendar.MINUTE);
		if((year>curYear)||(year==curYear&&month>curMonth)||(year==curYear&&month==curMonth&&date>curDay)||(year==curYear&&month==curMonth&&date==curDay&&hour>curHour)||(year==curYear&&month==curMonth&&date==curDay&&hour==curHour&&minute>curMinute)){
			isLegal = true;
		}else{
			isLegal = false;
		}
		return isLegal;
	}
	
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {

	        if (requestCode == REQUEST_CONTACT) {

	            if (resultCode == RESULT_OK) {

	                if (data == null) {
	                    return;
	                }    
	                Uri result = data.getData();
	                String phoneNumber = null;
	                Cursor c =  managedQuery(result, null, null, null, null);
	                if (c.moveToFirst()) {
	                	String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
	                	String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	                  String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
	                  if(hasPhone.equals("1")){
	                	  hasPhone = "true";
	                  }
	                  else{
	                	  hasPhone = "false";
	                  }
	                  if (Boolean.parseBoolean(hasPhone)) 
	                  {
	                   Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,null, null);
	                   while (phones.moveToNext()) 
	                   {
	                     phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	                   }
	                   phone.setText(phone.getText().toString() + " " + phoneNumber);
//	                   phone.setText(phoneNumber);
	                   phones.close();
	                  }
	                }
	            }
	        }
	  }
	 

	    
}
