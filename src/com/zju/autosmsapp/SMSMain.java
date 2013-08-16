package com.zju.autosmsapp;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;

public class SMSMain {
	public static final String SMS_ID = "sms_id";
	private final static String DM12 = "yyyy/MM/dd h:mm aa";
    private final static String DM24 = "yyyy/MM/dd k:mm";
	public static final String SMS_RAW_DATA = "intent.extra.sms_raw";
	final static String PREF_SNOOZE_ID = "snooze_id";
    final static String PREF_SNOOZE_TIME = "snooze_time";

    public static void addSMS(Context context, SMS sms){
		ContentValues values = createContentValues(sms);
		Uri uri = context.getContentResolver().insert(SMS.Columns.CONTENT_URI, values);
		sms.id = (int) ContentUris.parseId(uri);
		long timeInMillis = calculateTime(sms);//
		if(true){
			clearSnoozeIfNeeded(context,timeInMillis);//
		}
		Log.i("SMSMain.addSMS.sms.id", sms.id + " ");
		setNextAlert(context);
	}
    
    public static void updateSMS(Context context, SMS sms){
		ContentValues values = createContentValues(sms);
		ContentResolver resolver = context.getContentResolver();
		resolver.update(ContentUris.withAppendedId(SMS.Columns.CONTENT_URI, sms.id), values, null, null);
		long timeInMillis = calculateTime(sms);//
		if(true){
			clearSnoozeIfNeeded(context,timeInMillis);//
		}
		Log.i("SMSMain.updateSMS.sms.id", sms.id + " ");
		setNextAlert(context);
	}
    
    public static void deleteSMS(Context context, int smsId){
    	if (smsId == -1) return;

        ContentResolver contentResolver = context.getContentResolver();
        /* If alarm is snoozing, lose it */
        //disableSnoozeAlert(context, alarmId);

        Uri uri = ContentUris.withAppendedId(SMS.Columns.CONTENT_URI, smsId);
        contentResolver.delete(uri, "", null);
        setNextAlert(context);
    	
    }
	
	private static void clearSnoozeIfNeeded(Context context,long sendTime){///
		SharedPreferences prefs = context.getSharedPreferences(AutoSMSMainActivity.PREFERENCES, 0);
		long snoozeTime = prefs.getLong(PREF_SNOOZE_TIME, 0);
		if(sendTime < snoozeTime){
			clearSnoozePreference(context,prefs);
		}
	}
	
	private static void clearSnoozePreference(Context context,SharedPreferences prefs){///
		int smsId = prefs.getInt(PREF_SNOOZE_ID, -1);
		if(smsId != -1){
			NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(smsId);
		}
		
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(PREF_SNOOZE_ID);
		editor.remove(PREF_SNOOZE_TIME);
		editor.apply();
	}
	public static ContentValues createContentValues(SMS sms){
		ContentValues values = new ContentValues(8);
		values.put(SMS.Columns.PHONE, sms.phone);
		values.put(SMS.Columns.MESSAGE, sms.message);
		values.put(SMS.Columns.YEAR, sms.year);
		values.put(SMS.Columns.MONTH, sms.month);
		values.put(SMS.Columns.DATE, sms.date);
		values.put(SMS.Columns.HOUR, sms.hour);
		values.put(SMS.Columns.MINUTES, sms.minutes);
		values.put(SMS.Columns.SEND_TIME, sms.sendTime);
		Log.i("SMSMain.createContentValues",sms.phone + "/" + sms.message + "/" + sms.year + "/" + sms.month + "/" + sms.date + "/" + sms.hour + "/" + sms.minutes + "/" + sms.sendTime);
		return values;
	}


	public static void setNextAlert(Context context){
		if(!enableSnoozeAlert(context)){
			SMS sms = calculateNextSMS(context);
			//
			if(sms==null)
				disableAlert(context);
			else{
				enableAlert(context,sms,sms.sendTime);
//				Log.i("SMSMain.setNextAlert", sms.phone + "/" + sms.message + "/" + sms.year + "/" + sms.month + "/" + sms.date + "/" + sms.hour + "/" + sms.minutes + "/" + sms.sendTime);
			}
		}
	}
	
	private static boolean enableSnoozeAlert(Context context){
		SharedPreferences prefs = context.getSharedPreferences(AutoSMSMainActivity.PREFERENCES, 0);
		int id = prefs.getInt(PREF_SNOOZE_ID, -1);
		if(id==-1){
			return false;
		}
		long time = prefs.getLong(PREF_SNOOZE_TIME, -1);
		SMS sms = getSMS(context.getContentResolver(),id);
		if(sms == null){
			return false;
		}
		sms.sendTime = time;
		enableAlert(context,sms,time);
		return true;
	}
	
	public static SMS getSMS(ContentResolver contentResolver, int smsId) {
        Cursor cursor = contentResolver.query(
                ContentUris.withAppendedId(SMS.Columns.CONTENT_URI, smsId),
                SMS.Columns.SMS_QUERY_COLUMNS,
                null, null, null);
        SMS sms = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                sms = new SMS(cursor);
            }
            cursor.close();
        }
        return sms;
    }
	
	public static void enableAlert(Context context, final SMS sms,long atTimeInMillis){
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context,SMSReceiver.class);
		intent.setAction("SMSReceiver");
		
		Parcel out = Parcel.obtain();//??
		sms.writeToParcel(out, 0);//??
		out.setDataPosition(0);//??
		intent.putExtra(SMS_RAW_DATA, out.marshall());//??
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		alarmManager.set(AlarmManager.RTC_WAKEUP, sms.sendTime, pendingIntent);
		
//		Log.i("SMSMain.enableAlert", sms.phone + "/" + sms.message + "/" + sms.year + "/" + sms.month + "/" + sms.date + "/" + sms.hour + "/" + sms.minutes + "/" + sms.sendTime);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(sms.sendTime);
		String timeString = formatDayAndTime(context,calendar);
		saveNextAlarm(context,timeString);
	}
	
	public static void saveNextAlarm(final Context context,String timeString){
		Settings.System.putString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED, timeString);
	}
	
	private static String formatDayAndTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? DM24 : DM12;
        return (c == null) ? "" : (String)DateFormat.format(format, c);
    }
	
	static boolean get24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }
	
	public static SMS calculateNextSMS(Context context){
//		ArrayList<SMS> smsList = new ArrayList<SMS>();
		SMS sms = null;
		long minTime = Long.MAX_VALUE;
		long now = System.currentTimeMillis();
		Cursor cursor = getFilteredSMSCursor(context.getContentResolver());
		if(cursor!=null){
			if(cursor.moveToFirst()){
				do{
					SMS a = new SMS(cursor);
					if(a.sendTime == 0){
						a.sendTime = calculateTime(a);
						Log.i("SMSMain.calculateNextSMS", "a.sendTime ==0");
					}
					if(a.sendTime < now){
						continue;
					}
					if(a.sendTime < minTime){//考虑在同一时间同时发送的多条短信
						minTime = a.sendTime;
						Log.i("minTime", minTime + " ");
						sms = a;
//						smsList.add(a);
					}
				}while(cursor.moveToNext());
			}
			cursor.close();
		}
		else{
			sms = null;
		}
		return sms;
	}
	
	public static long calculateTime(SMS sms){	
		
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.YEAR, sms.year);
		c.set(Calendar.MONTH,sms.month);
		c.set(Calendar.DATE,sms.date);
		c.set(Calendar.HOUR_OF_DAY, sms.hour);
		c.set(Calendar.MINUTE, sms.minutes);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		Log.i("SMSMain.calculateTime.sms.id", sms.id + " ");
		Log.i("SMSMain.calculateTime", sms.phone + "/" + sms.message + "/" + sms.year + "/" + sms.month + "/" + sms.date + "/" + sms.hour + "/" + sms.minutes + "/" + sms.sendTime);
		return c.getTimeInMillis();
	}
	
	
	static void disableAlert(Context context) {
        AlarmManager am = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(
                context, 0, new Intent(context,SMSReceiver.class),
                PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
        //setStatusBarIcon(context, false);
        saveNextAlarm(context, "");
    }
	
	private static Cursor getFilteredSMSCursor(ContentResolver contentResolver){
		if(contentResolver.query(SMS.Columns.CONTENT_URI, SMS.Columns.SMS_QUERY_COLUMNS, null, null, null)!=null)
			return contentResolver.query(SMS.Columns.CONTENT_URI, SMS.Columns.SMS_QUERY_COLUMNS, null, null, null);
		else
			return null;
	}
	
	public static Cursor getSMSCursor(ContentResolver contentResolver) {
		return contentResolver.query(
                SMS.Columns.CONTENT_URI, SMS.Columns.SMS_QUERY_COLUMNS,
                null, null, SMS.Columns.DEFAULT_SORT_ORDER);
	}
}
