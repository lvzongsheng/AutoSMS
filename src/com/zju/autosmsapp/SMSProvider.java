package com.zju.autosmsapp;

import com.zju.autosmsapp.SMS.Columns;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


//database 
public class SMSProvider extends ContentProvider {
    private SQLiteOpenHelper mOpenHelper;

    private static final int SMS = 1;
    private static final int SMS_ID = 2;
    private static final String table_name = "sms";
    private static final String autority = "com.zju.autosmsapp";
    private static final UriMatcher sURLMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sURLMatcher.addURI(autority, table_name, SMS);
        sURLMatcher.addURI(autority, table_name+"/#", SMS_ID);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "sms.db";
        private static final int DATABASE_VERSION = 1;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE "+table_name+" (" +
                       "_id INTEGER PRIMARY KEY," +
                       Columns.PHONE + " TEXT, " +
                       Columns.MESSAGE + " TEXT, " +
                       Columns.YEAR + " INTEGER, " +
                       Columns.MONTH + " INTEGER, " +
                       Columns.DATE + " INTEGER, " +
                       Columns.HOUR + " INTEGER, " +
                       Columns.MINUTES + " INTEGER, " +
                       Columns.SEND_TIME + " LONG)"
                       );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
            if (true) Log.v("lvzongsheng",
                    "Upgrading "+table_name+" database from version " +
                    oldVersion + " to " + currentVersion +
                    ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+table_name);
            onCreate(db);
        }
    }

    public SMSProvider() {
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection,
            String[] selectionArgs, String sort) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // Generate the body of the query
        int match = sURLMatcher.match(url);
        switch (match) {
            case SMS:
                qb.setTables(table_name);
                break;
            case SMS_ID:
                qb.setTables(table_name);
                qb.appendWhere("_id=");
                qb.appendWhere(url.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor ret = qb.query(db, projectionIn, selection, selectionArgs,
                              null, null, sort);

        if (ret == null) {
            if (true) Log.v("lvzongsheng", table_name+".query: failed");
        } else {
            ret.setNotificationUri(getContext().getContentResolver(), url);
        }

        return ret;
    }

    @Override
    public String getType(Uri url) {
        int match = sURLMatcher.match(url);
        switch (match) {
            case SMS:
                return "vnd.android.cursor.dir/"+table_name;
            case SMS_ID:
                return "vnd.android.cursor.item/"+table_name;
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int count;
        long rowId = 0;
        int match = sURLMatcher.match(url);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (match) {
            case SMS_ID: {
                String segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                count = db.update(table_name, values, "_id=" + rowId, null);
                break;
            }
            default: {
                throw new UnsupportedOperationException(
                        "Cannot update URL: " + url);
            }
        }
        Log.v("lvzongsheng", "*** notifyChange() rowId: " + rowId + " url " + url);
        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        if (sURLMatcher.match(url) != SMS) {
            throw new IllegalArgumentException("Cannot insert into URL: " + url);
        }

        ContentValues values = new ContentValues(initialValues);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(table_name, null, values);
        if (rowId < 0) {
            throw new SQLException("Failed to insert row into " + url);
        }
        Log.v("lvzongsheng", "Added sms rowId = " + rowId);

        Uri newUrl = ContentUris.withAppendedId(com.zju.autosmsapp.SMS.Columns.CONTENT_URI, rowId);
        getContext().getContentResolver().notifyChange(newUrl, null);
        return newUrl;
    }

    public int delete(Uri url, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        long rowId = 0;
        switch (sURLMatcher.match(url)) {
            case SMS:
                count = db.delete(table_name, where, whereArgs);
                break;
            case SMS_ID:
                String segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                if (TextUtils.isEmpty(where)) {
                    where = "_id=" + segment;
                } else {
                    where = "_id=" + segment + " AND (" + where + ")";
                }
                count = db.delete(table_name, where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + url);
        }

        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }
}
