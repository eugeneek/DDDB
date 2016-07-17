package com.example.eugene.dddb.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "DDList.db";
    private static final String TABLE_NAME = "items";
    private static final String ID = "_id";
    private static final String VALUE = "value";
    private static final String PREV_ID = "prev_id";
    private static final String NEXT_ID = "next_id";

    private static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ( "
            + ID + " INTEGER PRIMARY KEY, "
            + VALUE + " TEXT, "
            + PREV_ID + " INTEGER,"
            + NEXT_ID + " INTEGER )";
    private static final String SQL_DROP = "DROP TABLE IS EXISTS " + TABLE_NAME ;

    DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP);
        db.execSQL(SQL_CREATE);
    }
}
