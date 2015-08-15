package com.befun.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper{
	public MyDatabaseHelper(Context context,String name,int version){
		super(context,name,null,version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table table_roster(_id INTEGER PRIMARY KEY AUTOINCREMENT,"+
				"username TEXT UNIQUE ON CONFLICT REPLACE,"+
				"gender TEXT,"+
				"nickname TEXT)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}
