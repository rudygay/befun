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

public class FriendProvider extends ContentProvider {
	
	public static final String AUTHORITY = "com.befun.provider.friend";
	public static final String TABLE_FRIEND = "friends";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_FRIEND);
	public static final String _ID = "_id";
	private static final UriMatcher URI_MATCHER = new UriMatcher(
			UriMatcher.NO_MATCH);
	private static final int CONTACTS = 1;
	private static final int CONTACT_ID = 2;
	private Handler mNotifyHandler = new Handler();
	private Runnable mNotifyChange = new Runnable() {
		public void run() {
			Log.d("friendprovider", "notifying change");
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);
		}
	};
	static {
		URI_MATCHER.addURI(AUTHORITY, "friends", CONTACTS);
		URI_MATCHER.addURI(AUTHORITY, "friend/#", CONTACT_ID);	
	}
	
	private SQLiteOpenHelper mOpenHelper;
	public FriendProvider() {
		
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new MyDatabaseHelper(getContext(), "friend.db3", 1);
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (URI_MATCHER.match(uri)) {
		case CONTACTS:
			return db.query(TABLE_FRIEND, projection
					, selection, selectionArgs, null, null, sortOrder);
			
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public String getType(Uri url) {
		int match = URI_MATCHER.match(url);
		switch (match) {
		case CONTACTS:
			return "vnd.android.cursor.dir/com.befun.friend";
		case CONTACT_ID:
			return "vnd.android.cursor.item/com.befun.friend";
		default:
			throw new IllegalArgumentException("Unknown URL");
		}
	}

	@Override
	public Uri insert(Uri url, ContentValues values) {
		if (URI_MATCHER.match(url) != CONTACTS) {
			throw new IllegalArgumentException("Cannot insert into URL: " + url);
		}
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(TABLE_FRIEND, _ID, values);
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

		case CONTACTS:
			count = db.delete(TABLE_FRIEND, where, whereArgs);
			break;

		case CONTACT_ID:
			String segment = url.getPathSegments().get(1);

			if (TextUtils.isEmpty(where)) {
				where = "_id=" + segment;
			} else {
				where = "_id=" + segment + " AND (" + where + ")";
			}

			count = db.delete(TABLE_FRIEND, where, whereArgs);
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
		case CONTACTS:
			count = db.update(TABLE_FRIEND, values, selection, selectionArgs);
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
			db.execSQL("create table "+TABLE_FRIEND+"(_id INTEGER PRIMARY KEY AUTOINCREMENT,"+
					"username TEXT UNIQUE ON CONFLICT REPLACE,"+
					"gender TEXT,"+
					"is_read INTEGER,"+
					"nickname TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
		}
	}
}
