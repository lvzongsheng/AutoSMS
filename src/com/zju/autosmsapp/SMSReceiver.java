package com.zju.autosmsapp;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver{
	public void onReceive(Context context, Intent intent) {  
		Log.i("SMSReceiver.onReceive", intent.toString());
		SMS sms = null;
        final byte[] data = intent.getByteArrayExtra(SMSMain.SMS_RAW_DATA);
        
        if (data != null) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            sms = SMS.CREATOR.createFromParcel(in);
        }

        if (sms == null) {
            SMSMain.setNextAlert(context);
            return;
        }
        //SharedPreferences sharedPreferences = context.getSharedPreferences(  
         //       AutoSMSMainActivity.PREFERENCES, Activity.MODE_PRIVATE);  
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int date = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);  
        int minute = Calendar.getInstance().get(Calendar.MINUTE);  
  
        /*String time = sharedPreferences.getString(year+"/"+month+"/"+date +" "+ hour + ":" + minute, null);// ���������������  
        String phone = sharedPreferences.getString("phone", null);  
        String message = sharedPreferences.getString("message", null);  
        */
        Log.i("SMSReceiver/currentTime", year + "-" + month +"-" + date + "-" + hour + "-" + minute );
        Log.i("SMSReceiver/sms.time", sms.year + "-" + sms.month + "-" + sms.date + "-" +  sms.hour + "-" + sms.minutes);
        if (sms.year==year&&sms.month==month&&sms.date==date&&sms.hour==hour&&sms.minutes==minute) {  
  
            Toast.makeText(context, "定时短信发送成功", Toast.LENGTH_LONG).show(); 
            
            String[] phones = sms.phone.trim().split(" ");
            String message = sms.message;
            for(int i = 0; i<phones.length;++i){
            	String phone = phones[i].trim();
            	sendMsg(phone, message);
                
                ContentValues values = new ContentValues();
                values.put("date",System.currentTimeMillis());
                values.put("read", 0);
                values.put("type",2);
                values.put("address",phone);
                values.put("body",message);
                context.getContentResolver().insert(Uri.parse("content://sms"),values);
            }
            SMSMain.deleteSMS(context, sms.id);
            if(SMSMain.calculateNextSMS(context)!=null)
            	SMSMain.setNextAlert(context);
        }  
    }  
  
    private void sendMsg(String number, String message) {
    	Log.i("SMSReceiver.sendMsg", number + " " + message);
        SmsManager smsManager = SmsManager.getDefault();  
        smsManager.sendTextMessage(number, null, message, null, null);  
    } 
}
