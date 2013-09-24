package com.zju.autosmsapp;

import java.util.Calendar;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AutoSMSMainActivity extends Activity {
	public static final String PREFERENCES = "SMS";
	
	private SharedPreferences mPrefs;
	private ListView smsList;
	private Cursor mCursor;
	private LayoutInflater mFactory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_smsmain);
		mPrefs = getSharedPreferences(PREFERENCES, 0);
		mCursor = SMSMain.getSMSCursor(getContentResolver());
		smsList = (ListView)findViewById(R.id.smses_list);
		mFactory = LayoutInflater.from(this);
		
		AlarmTimeAdapter adapter = new AlarmTimeAdapter(this, mCursor);
		smsList.setAdapter(adapter);
		smsList.setVerticalScrollBarEnabled(true);
		smsList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> adapterView, View v, int pos,
					long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(AutoSMSMainActivity.this, SetSMS.class);
		        intent.putExtra(SMSMain.SMS_ID, (int) id);
		        startActivity(intent);
				
			}
			
		});
		//smsList.setOnCreateContextMenuListener(this);
		smsList.setOnCreateContextMenuListener(new OnCreateContextMenuListener(){

			@Override
			public void onCreateContextMenu(ContextMenu menu, View view,
					ContextMenuInfo menuInfo) {
				// TODO Auto-generated method stub
				getMenuInflater().inflate(R.menu.context_menu, menu);

		        // Use the current item to create a custom view for the header.
		        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		        final Cursor c =
		                (Cursor) smsList.getAdapter().getItem((int) info.position);
		        final SMS sms = new SMS(c);
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
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}
	
	public boolean onContextItemSelected(final MenuItem item) {
        final AdapterContextMenuInfo info =
                (AdapterContextMenuInfo) item.getMenuInfo();
        final int id = (int) info.id;
        // Error check just in case.
        if (id == -1) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.delete_sms:
                // Confirm that the alarm will be deleted.
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete_sms))
                        .setMessage(getString(R.string.delete_sms_confirm))
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
	
	private class AlarmTimeAdapter extends CursorAdapter {
        @SuppressWarnings("deprecation")
		public AlarmTimeAdapter(Context context, Cursor cursor) {
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

            // set the alarm text
            final Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, sms.year);
            c.set(Calendar.MONTH, sms.month);
            c.set(Calendar.DATE, sms.date);
            c.set(Calendar.HOUR_OF_DAY, sms.hour);
            c.set(Calendar.MINUTE, sms.minutes);
            smsShow.updateTime(c);
            smsShow.setTypeface(Typeface.DEFAULT);

            // Display the label
            TextView phoneShow = (TextView)view.findViewById(R.id.phoneShow);
            phoneShow.setText(sms.phone);
            phoneShow.setVisibility(View.VISIBLE);
            TextView messageShow = (TextView)view.findViewById(R.id.messageShow);
    		messageShow.setText(sms.message);
    		messageShow.setVisibility(View.VISIBLE);
            
        }
    };

}
