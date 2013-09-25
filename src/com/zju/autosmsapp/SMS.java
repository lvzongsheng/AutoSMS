package com.zju.autosmsapp;

import java.util.Calendar;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

public final class SMS implements Parcelable{
	
	public static final Parcelable.Creator<SMS> CREATOR
    = new Parcelable.Creator<SMS>() {
        public SMS createFromParcel(Parcel p) {
            return new SMS(p);
        }

        public SMS[] newArray(int size) {
            return new SMS[size];
        }
    };

	public int describeContents() {
		return 0;
	}
	
	public void writeToParcel(Parcel p, int flags) {
		p.writeInt(id);
		p.writeString(phone);
		p.writeString(message);
		p.writeInt(year);
		p.writeInt(month);
		p.writeInt(date);
		p.writeInt(hour);
		p.writeInt(minutes);
		p.writeLong(sendTime);
	}
	
	public static class Columns implements BaseColumns {
        public static final Uri CONTENT_URI =
                Uri.parse("content://com.zju.autosmsapp/sms");
        
        public static final String YEAR = "year";
        public static final String MONTH = "month";
        public static final String DATE = "date";
        public static final String HOUR = "hour";
        public static final String MINUTES = "minutes";
        public static final String SEND_TIME = "sendtime";
        public static final String MESSAGE = "message";
        public static final String PHONE = "phone";
        public static final String ENABLED = "enabled";
        public static final String DEFAULT_SORT_ORDER =
           YEAR + ", " + MONTH + ", " + DATE + ", " + HOUR + ", " + MINUTES + " ASC";

        public static final String WHERE_ENABLED = ENABLED + "=1";

        static final String[] SMS_QUERY_COLUMNS = {
            _ID, PHONE,MESSAGE,YEAR,MONTH,DATE,HOUR, MINUTES, SEND_TIME };
        public static final int SMS_ID_INDEX = 0;
        public static final int SMS_PHONE_INDEX = 1;
        public static final int SMS_MESSAGE_INDEX = 2;
        public static final int SMS_YEAR_INDEX = 3;
        public static final int SMS_MONTH_INDEX = 4;
        public static final int SMS_DATE_INDEX = 5;
        public static final int SMS_HOUR_INDEX = 6;
        public static final int SMS_MINUTES_INDEX = 7;
        public static final int SMS_SEND_TIME = 8;
        
    }

	public String phone;
	public String message;
	public int year;
	public int month;
	public int date;
	public int hour;
	public int minutes;
	public int id;
	public long sendTime;
	
    public SMS(Cursor c) {
        id = c.getInt(Columns.SMS_ID_INDEX);
        year = c.getInt(Columns.SMS_YEAR_INDEX);
        month = c.getInt(Columns.SMS_MONTH_INDEX);
        date = c.getInt(Columns.SMS_DATE_INDEX);
        hour = c.getInt(Columns.SMS_HOUR_INDEX);
        minutes = c.getInt(Columns.SMS_MINUTES_INDEX);
        phone = c.getString(Columns.SMS_PHONE_INDEX);
        message = c.getString(Columns.SMS_MESSAGE_INDEX);
        sendTime = c.getLong(Columns.SMS_SEND_TIME);
    }
    
    public SMS(Parcel p) {
        id = p.readInt();
        phone = p.readString();
        message = p.readString();
        year = p.readInt();
        month = p.readInt();
        date = p.readInt();
        hour = p.readInt();
        minutes = p.readInt();
        sendTime = p.readLong();
    }

    public SMS() {
    	id = -1;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        date = c.get(Calendar.DATE);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minutes = c.get(Calendar.MINUTE);
    }
}
