package com.zju.autosmsapp;

import java.util.Calendar;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AutoSMSMainActivity extends Activity {
	public static final String PREFERENCES = "SMS";
	
	private ListView smsList;
	private Cursor mCursor;
	private LayoutInflater mFactory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_smsmain);
		mCursor = SMSMain.getSMSCursor(getContentResolver());
		smsList = (ListView)findViewById(R.id.smses_list);
		mFactory = LayoutInflater.from(this);
		
		SMSTimeAdapter adapter = new SMSTimeAdapter(this, mCursor);
		smsList.setAdapter(adapter);
		smsList.setVerticalScrollBarEnabled(true);
		smsList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> adapterView, View v, int pos,
					long id) {
				Intent intent = new Intent(AutoSMSMainActivity.this, SetSMS.class);
		        intent.putExtra(SMSMain.SMS_ID, (int) id);
		        startActivity(intent);
				
			}
			
		});
		
		View addSMS = findViewById(R.id.add_sms);
		
		addSMS.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				addNewSMS();
			}
			
		});
	}
	
	public void addNewSMS(){
		startActivity(new Intent(AutoSMSMainActivity.this, SetSMS.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	public boolean onContextItemSelected(final MenuItem item) {
        final AdapterContextMenuInfo info =
                (AdapterContextMenuInfo) item.getMenuInfo();
        final int id = (int) info.id;
        if (id == -1) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.delete_sms:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete_sms))
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d,
                                            int w) {
                                        SMSMain.deleteSMS(AutoSMSMainActivity.this, id);
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;

            case R.id.edit_sms:
                Intent intent = new Intent(this, SetSMS.class);
                intent.putExtra(SMSMain.SMS_ID, id);
                startActivity(intent);
                return true;

            default:
                break;
        }
        return super.onContextItemSelected(item);
    }
	
	private class SMSTimeAdapter extends CursorAdapter {
        @SuppressWarnings("deprecation")
		public SMSTimeAdapter(Context context, Cursor cursor) {
        	super(context, cursor);
        }

        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View ret = mFactory.inflate(R.layout.sms_time, parent, false);
            
            SMSShow smsShow =
                    (SMSShow) ret.findViewById(R.id.smsShow);
            smsShow.setLive(false);
            return ret;
        }

        public void bindView(View view, Context context, Cursor cursor) {
            final SMS sms = new SMS(cursor);

            SMSShow smsShow =
                    (SMSShow) view.findViewById(R.id.smsShow);

            final Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, sms.year);
            c.set(Calendar.MONTH, sms.month);
            c.set(Calendar.DATE, sms.date);
            c.set(Calendar.HOUR_OF_DAY, sms.hour);
            c.set(Calendar.MINUTE, sms.minutes);
            smsShow.updateTime(c);
            smsShow.setTypeface(Typeface.DEFAULT);

            TextView phoneShow = (TextView)view.findViewById(R.id.phoneShow);
            phoneShow.setText(sms.phone);
            phoneShow.setVisibility(View.VISIBLE);
            TextView messageShow = (TextView)view.findViewById(R.id.messageShow);
    		messageShow.setText(sms.message);
    		messageShow.setVisibility(View.VISIBLE);
            
        }
    };

}
