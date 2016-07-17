package com.example.eugene.dddb.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.HashSet;

public class ItemsProvider extends ContentProvider {

    public static final String AUTHORITY = "com.example.eugene.dddb.ItemStorage";
    private static final String PATH = "items";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
    private static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY
            + "." + PATH;
    private static final String ITEM_CONTENT_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY
            + "." + PATH;
    private static final int URI_ITEMS = 1;
    private static final int URI_ITEMS_ID = 2;

    private static final String TABLE_NAME = "items";
    public static final String ID = "_id";
    public static final String VALUE = "value";
    public static final String PREV_ID = "prev_id";
    public static final String NEXT_ID = "next_id";

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, PATH, URI_ITEMS);
        uriMatcher.addURI(AUTHORITY, PATH + "/#", URI_ITEMS_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        checkColumns(projection);
        switch (uriMatcher.match(uri)) {
            case URI_ITEMS:
                if (sortOrder == null || sortOrder.isEmpty())
                    sortOrder = ID +" ASC";
                break;
            case URI_ITEMS_ID:
                selection = selection + ID + " = " + uri.getLastPathSegment();
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case URI_ITEMS:
                return CONTENT_TYPE;
            case URI_ITEMS_ID:
                return ITEM_CONTENT_TYPE;
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) != URI_ITEMS)
            throw new IllegalArgumentException("Unsupported URI for insert operation: " + uri);

        db = dbHelper.getWritableDatabase();
        long rowID = db.insert(TABLE_NAME, null, values);
        Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
        getContext().getContentResolver().notifyChange(resultUri, null);

        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_ITEMS:
                break;
            case URI_ITEMS_ID:
                String id = uri.getLastPathSegment();
                if (selection == null || selection.isEmpty()) {
                    selection = ID + " = " + id;
                } else {
                    selection = selection + " AND " + ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int cnt = db.delete(TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_ITEMS:
                break;
            case URI_ITEMS_ID:
                String id = uri.getLastPathSegment();
                if (selection == null || selection.isEmpty()) {
                    selection = ID + " = " + id;
                } else {
                    selection = selection + " AND " + ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        db = dbHelper.getWritableDatabase();
        int cnt = db.update(TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    private void checkColumns(String[] projection) {
        String[] available = { ID, VALUE, PREV_ID, NEXT_ID };
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<>(
                    Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<>(
                    Arrays.asList(available));
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException(
                        "Unknown columns in projection");
            }
        }
    }
}
