package com.befun.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

public class MessageProvider extends ContentProvider {
	
	public static final String AUTHORITY = "com.befun.provider.message";
	public static final String TABLE_MESSAGE = "messages";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_MESSAGE);
	public static final String _ID = "_id";
	private static final UriMatcher URI_MATCHER = new UriMatcher(
			UriMatcher.NO_MATCH);
	
	public static final int MESSAGES = 1;
	public static final int MESSAGES_ID = 2;
	
	public static final String MESSAGE_DIREC ="fromorto";
	public static final String MESSAGE_TYPE ="type";
	public static final String MESSAGE_CONTENT = "message_content";
	public static final String MESSAGE_DATE = "date";
	public static final String MESSAGE_IMG = "picture";
	
	public static final int INCOMING = 0;
	public static final int OUTGOING = 1;
	
	public static final int MESSAGE_TXT = 0;
	public static final int MESSAGE_YY = 3;
	public static final int MESSAGE_PIC = 1;
	public static final int MESSAGE_DELETE = 9;
	public static final int MESSAGE_CONTROL = 2;
	public static final int MESSAGE_BIAOQING = 4;
	
	private Handler mNotifyHandler = new Handler();
	private Runnable mNotifyChange = new Runnable() {
		public void run() {
			Log.d("friendprovider", "notifying change");
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);
		}
	};
	static {
		URI_MATCHER.addURI(AUTHORITY, "messages", MESSAGES);
		URI_MATCHER.addURI(AUTHORITY, "message/#", MESSAGES_ID);	
	}
	
	private SQLiteOpenHelper mOpenHelper;
	public MessageProvider() {
		
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new MyDatabaseHelper(getContext(), "message.db3", 1);
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (URI_MATCHER.match(uri)) {
		case MESSAGES:
			return db.query(TABLE_MESSAGE, projection
					, selection, selectionArgs, null, null, sortOrder);
			
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public String getType(Uri url) {
		int match = URI_MATCHER.match(url);
		switch (match) {
		case MESSAGES:
			return "vnd.android.cursor.dir/com.befun.message";
		case MESSAGES_ID:
			return "vnd.android.cursor.item/com.befun.message";
		default:
			throw new IllegalArgumentException("Unknown URL");
		}
	}

	@Override
	public Uri insert(Uri url, ContentValues values) {
		if (URI_MATCHER.match(url) != MESSAGES) {
			throw new IllegalArgumentException("Cannot insert into URL: " + url);
		}
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(TABLE_MESSAGE, _ID, values);
		if (rowId < 0) {
			throw new SQLException("Failed to insert row into " + url);
		}

		Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
		notifyChange();
		return noteUri;
	}

	@Override
	public int delete(Uri url, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (URI_MATCHER.match(url)) {

		case MESSAGES:
			count = db.delete(TABLE_MESSAGE, where, whereArgs);
			break;

		case MESSAGES_ID:
			String segment = url.getPathSegments().get(1);

			if (TextUtils.isEmpty(where)) {
				where = "_id=" + segment;
			} else {
				where = "_id=" + segment + " AND (" + where + ")";
			}

			count = db.delete(TABLE_MESSAGE, where, whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Cannot delete from URL: " + url);
		}

		getContext().getContentResolver().notifyChange(url, null);
		notifyChange();
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count;
		int match = URI_MATCHER.match(uri);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		switch (match) {
		case MESSAGES:
			count = db.update(TABLE_MESSAGE, values, selection, selectionArgs);
			break;
		default:
			throw new UnsupportedOperationException("Cannot update URL: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	long last_notify = 0;

	private void notifyChange() {
		mNotifyHandler.removeCallbacks(mNotifyChange);
		long ts = System.currentTimeMillis();
		if (ts > last_notify + 500)
			mNotifyChange.run();
		else
			mNotifyHandler.postDelayed(mNotifyChange, 200);
		last_notify = ts;
	}
	private class MyDatabaseHelper extends SQLiteOpenHelper{
		public MyDatabaseHelper(Context context,String name,int version){
			super(context,name,null,version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table "+TABLE_MESSAGE+"(_id INTEGER PRIMARY KEY AUTOINCREMENT,"+
					"username TEXT,"+
					MESSAGE_DIREC+" INTEGER,"+
					MESSAGE_TYPE+" INTEGER,"+
					MESSAGE_DATE+" INTEGER,"+
					MESSAGE_CONTENT+" TEXT);");
			db.execSQL("CREATE INDEX fName on "+TABLE_MESSAGE +"(username);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
		}
	}
}
